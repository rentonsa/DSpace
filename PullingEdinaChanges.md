# Instructions for pulling Edina's changes into our datashare branch
## First steps
**Clone our DSpace fork if you haven't done already**

EITHER `git clone git@github.com:UoEMainLibrary/DSpace.git` (if you have a certificate set up with Github)

OR `git clone https://github.com/UoEMainLibrary/DSpace.git` (if you don't)

**Add the Edina repository as a remote** -  _You only need to do this once_

EITHER `git remote add edina git@github.com:edina/DSpace.git`

OR `git remote add edina https://github.com/edina/DSpace.git`

## To pull changes from Edina -> Us
**Checkout our datashare branch**

`git checkout datashare-dspace-5.x`

**Fetch the Edina remote**

`git fetch edina`

**Merge Edina branch**

`git merge edina/datashare-dspace-5.x`

**Push back to our repo**

`git push`
