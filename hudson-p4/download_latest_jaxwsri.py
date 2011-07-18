#!/usr/bin/python

import urllib
#import xml
import xml.dom.minidom
import os

def getText(nodelist):
    rc = []
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc.append(node.data)
    return ''.join(rc)

def _mkdir(newdir):
    """works the way a good mkdir should :)
        - already exists, silently complete
        - regular file in the way, raise an exception
        - parent directory(ies) does not exist, make them as well
    """
    if os.path.isdir(newdir):
        pass
    elif os.path.isfile(newdir):
        raise OSError("a file with the same name as the desired " \
                      "dir, '%s', already exists." % newdir)
    else:
        head, tail = os.path.split(newdir)
        if head and not os.path.isdir(head):
            _mkdir(head)
        #print "_mkdir %s" % repr(newdir)
        if tail:
            os.mkdir(newdir)

#todo unique or temp file name needed
HUDSON_XML_LOCAL_FILE = "/tmp/urlretrieve.xml"
urllib.urlretrieve("http://hudson-sca.us.oracle.com/view/JAX-WS/view/WLS/job/jaxws-ri-wls/api/xml", HUDSON_XML_LOCAL_FILE)

WORK_DIR="/tmp/jaxwstop4"
_mkdir(WORK_DIR);
P4_EXE_FILE= WORK_DIR + "/p4"
urllib.urlretrieve("http://home.us.oracle.com/p4/r09.1/bin.linux24x86/p4", P4_EXE_FILE)

os.chmod(P4_EXE_FILE, 0777)

dom = xml.dom.minidom.parse(HUDSON_XML_LOCAL_FILE )
lastSuccessfulBuild = dom.getElementsByTagName("lastSuccessfulBuild")[0]


urlToBuild = lastSuccessfulBuild.getElementsByTagName("url")[0]

libUrl =  "%sartifact/jaxws-ri/dist/jaxws-ri/lib/" %  getText(urlToBuild.childNodes)

print "libUrl is %s" % libUrl

localLibDir = "/tmp/libDir/"

_mkdir(localLibDir)

# small list fnames = ["jsr181-api.jar", "common.sdo.jar"]
fnames = ["jsr181-api.jar", "common.sdo.jar",
"activation.jar",  "jaxb-impl.src.zip", "management-api.jar", 
"common.sdo.jar",  "jaxb-xjc.jar",  "mimepull.jar", 
"eclipselink.jar",  "jaxb-xjc.src.zip ", "policy.jar", 
"FastInfoset.jar",  "jaxws-api.jar",  "resolver.jar", 
"gmbal-api-only.jar",  "jaxws-eclipselink-plugin.jar",  "saaj-api.jar", 
"ha-api.jar",  "jaxws-rt.jar",  "saaj-impl.jar", 
"http.jar",  "jaxws-tools.jar",  "stax-ex.jar", 
"javax.mail_1.4.jar", "jsr173_api.jar", "streambuffer.jar", 
"jaxb-api.jar",  "jsr181-api.jar",  "woodstox.jar", 
"jaxb-impl.jar",  "jsr250-api.jar", 
"jaxws-rt.src.zip", 
"jaxws-tools.src.zip", 
"jaxws-libs.src.zip"]

for fname in fnames:

	print "fname is " + fname
	urllib.urlretrieve(libUrl + fname, localLibDir + fname)
# end for


