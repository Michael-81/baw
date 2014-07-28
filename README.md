WPS processes provided by the federal waterways engineering and research institute (BAW).

## usage

**compilation**

1. `mvn clean install`


**deployment** //see also [aws/build.xml](aws/build.xml)

2. deploy regular 52n wps server 3.3.0-stable (or richwps server)
3. copy `aws/target/baw-aws-0.0.1-SNAPSHOT-full.jar` to  `/var/lib/tomcat7/webapps/wps/WEB-INF/lib`
4. copy `aws/wps_config.xml` to  `/var/lib/tomcat7/webapps/wps/config/`
5. `chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/wps`
6. `service tomcat7 restart`


**heads up:** if 52n wps server 3.3.0-stable is used, the wps_config needs to be altered.

## testing

[@see aws/testing README.md](aws/testing/README.md)

## contents

* `baw-aws::0.0.1-SNAPSHOT` //use -full.jar -- BAW process. 
* `baw-fft::0.0.1-SNAPSHOT`
* `baw-utils::0.0.1-SNAPSHOT`
* `baw-xml::0.0.1-SNAPSHOT`
