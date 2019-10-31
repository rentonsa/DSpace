package org.dspace.eperson;

import java.io.IOException;
import java.sql.SQLException;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;


public class DatashareAccountServiceImpl extends AccountServiceImpl {

	/** log4j log */
	private static Logger log = Logger.getLogger(DatashareAccountServiceImpl.class);
 
	/* DATASHARE code start */
	/**
	 * 
	 * @param context DSpace context
	 * @param email   Email Address
	 * @param uun     University user name
	 * @throws SQLException
	 * @throws IOException
	 * @throws MessagingException
	 * @throws AuthorizeException
	 */
	public void sendInfo(Context context, String email, String uun)
			throws SQLException, IOException, MessagingException, AuthorizeException {

		// See if a registration token already exists for this user
		RegistrationData rd = registrationDataService.findByEmail(context, email);

		// If it already exists, just re-issue it
		if (rd == null) {
			rd = registrationDataService.create(context);

			rd.setEmail(email);

			rd.setToken(uun);

			registrationDataService.update(context, rd);

			// This is a potential problem -- if we create the callback
			// and then crash, registration will get SNAFU-ed.
			// So FIRST leave some breadcrumbs
			if (log.isDebugEnabled()) {
				log.debug("Created callback " + rd.getID() + " with token " + rd.getToken() + " with email \"" + email
						+ "\"");
			}
		}

		sendEmail(context, email, true, rd);

	}

	
	/**
	 * Fetch the registration datya for a given token
	 * 
	 * @param context The DSpace context
	 * @param token   Registration token
	 * @return The registration data
	 * @throws SQLException 
	 */
	public RegistrationData getRegistrationData(Context context, String token) throws SQLException {
		return registrationDataService.findByToken(context, token);
	}
	/* DATASHARE code end */


}
