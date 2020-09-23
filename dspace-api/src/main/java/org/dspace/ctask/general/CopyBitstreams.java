package org.dspace.ctask.general;

/**
 * A curation job to move media files into a static tomcat folder
 *
 * @author Claire Knowles
 * Date: 26/01/16
 * Time: 10:51
 */



import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.ConfigurationManager;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

public class CopyBitstreams extends AbstractCurationTask
{

    // The results of processing this
    private List<String> results = null;

    // The status of this item

    private static final String PLUGIN_PREFIX = "copybitstreams";

    private int status = Curator.CURATE_UNSET;

    //loaded from config file in init
    private static String uploadDirectory = null;
    private static String mimetypes = null;
    private static String handleprefix = null;

    // The log4j logger for this class
    private static Logger log = Logger.getLogger(CheckBitstreamsExist.class);

    @Override
    public void init(Curator curator, String taskId) throws IOException {
        super.init(curator, taskId);

        uploadDirectory = ConfigurationManager.getProperty(PLUGIN_PREFIX, "uploadDirectory");
        mimetypes = ConfigurationManager.getProperty(PLUGIN_PREFIX, "mimetypes");
        handleprefix = ConfigurationManager.getProperty("handle.prefix");
        log.info("uploadDirectory " + uploadDirectory);
    }


    /**
     * Perform the bitstream metadata creation.
     *
     * @param dso The DSpaceObject to be checked
     * @return The curation task status of the checking
     */
    @Override
    public int perform(DSpaceObject dso) {
        // The results that we'll return
        StringBuilder results = new StringBuilder();

        // Unless this is an item, we'll skip this item
        status = Curator.CURATE_SKIP;
        logDebugMessage("The target dso is " + dso.getName());
        try {

            if (dso instanceof Item) {

                log.info("Using item " + dso.getHandle());
                Item item = (Item) dso;
                copyIfMimeType(results, item);
                //DSpace does not allow you to get all metadata for an item
            } else if (dso instanceof Collection) {
                log.info("Using Collection " + dso.getHandle());
                Collection collection = (Collection) dso;
                ItemIterator items = collection.getAllItems();
                while (items.hasNext()) {
                    Item item = items.next();
                    copyIfMimeType(results, item);
                }

            } else if (dso instanceof Community) {
                log.info("Using Community " + dso.getHandle());
                Community community = (Community) dso;
                Collection[] collections = community.getCollections();
                for (Collection collection : collections) {
                    ItemIterator items = collection.getAllItems();
                    while (items.hasNext()) {
                        Item item = items.next();
                        copyIfMimeType(results, item);
                    }
                }

            } else {
                log.info("dso is not an item, collection or community");
                System.out.println("dso is not an item, collection or community");
                results.append("DSpace Object is not an item, collection or community - SKIP\n");
            }
        } catch (SQLException se) {
            log.error(se.getMessage());
        }

        logDebugMessage("About to report: " + results.toString());
        System.out.println("About to report: " + results.toString());
        setResult(results.toString());
        report(results.toString());

        status = Curator.CURATE_SUCCESS;
        return status;
    }

    /**
     * Debugging logging if required
     *
     * @param message The message to log
     */
    private void logDebugMessage(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

    /**
     * Check the bitstream exists
     *
     * @param item    The item
     * @param results The results
     */
    private void copyIfMimeType(StringBuilder results, Item item) {
        results.append("Item : " + item.getHandle()+ "\n");
        try {
            for (Bundle bundle : item.getBundles()) {
                //TODO only run for ORIGINAL???
                if ("ORIGINAL".equals(bundle.getName())) {
                    for (Bitstream bitstream : bundle.getBitstreams()) {
                        results.append("Bitstream : " + bitstream.getName() + "\n");
                        if (mimetypes.contains(bitstream.getFormat().getMIMEType()))
                        {

                            String recordId = item.getHandle().replaceFirst(handleprefix + "/", "");
                            File destination = new File(uploadDirectory + "/" + recordId + "/" + bitstream.getSequenceID() + "/");
                            File fullDestination = new File(destination + "/" + bitstream.getName());
                            if (!fullDestination.exists())
                            {
                                if (!destination.isDirectory())
                                {
                                    try
                                    {
                                        System.out.println("directory path " + destination.toPath());
                                        boolean dirCreated = destination.mkdirs();
                                        if(!dirCreated)
                                        {
                                            results.append("Error creating : " + destination.getAbsolutePath() + "\n");
                                            System.out.println("Error creating : " + destination.getAbsolutePath() + "\n");
                                        }
                                    }
                                    catch(Exception e)
                                    {
                                         results.append(e.getMessage());
                                         System.out.println("Error could not create directory  " + destination.getAbsolutePath());
                                    }
                                }
                                if (destination.exists())
                                {

                                    System.out.println("Filepath " + fullDestination.toPath());

                                    try
                                    {
                                        results.append("Copying Bitstream : " + bitstream.getName() + "\n");
                                        System.out.println("Copying Bitstream : " + bitstream.getName() + "\n");
                                        //todo look at copy options
                                        Files.copy(bitstream.retrieve(), fullDestination.toPath()  , StandardCopyOption.REPLACE_EXISTING);
                                    }
                                    catch (IOException IOex) {
                                        results.append(IOex.getMessage());
                                    } catch (AuthorizeException Aex) {
                                        results.append(Aex.getMessage());
                                    }catch(Exception e)
                                    {
                                        results.append(e.getMessage());
                                        System.out.println("Error could not retrieve item for  " + item.getHandle());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException SQLex) {
            results.append(SQLex.getMessage());
        }
    }


}