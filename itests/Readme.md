## Integration testing with Fuse 6.x
This module builds on the rider osgi example and adds some automated integration tests built on top of [PaxExam](https://ops4j1.jira.com/wiki/display/PAXEXAM3/Pax+Exam) which is an awesome OSGI integration testing framework.

The module demonstrates a couple of ways to bootstrap and run tests:

* using the built in pax-exam reactors/probes to run the tests inside of the container and be able to take advantage of service injection
* using the _server_ mode attached to the maven lifecycle using the [pax-exam maven plugin](https://ops4j1.jira.com/wiki/display/PAXEXAM3/Exam+Maven+Plugin)
* how to bootstrap external resources (eg, activemq broker) before tests commence

I've also put together a blog that takes you step by step through this: [http://blog.christianposta.com/integration-testing-jboss-fuse-6-x-with-pax-exam-part-i/](http://blog.christianposta.com/integration-testing-jboss-fuse-6-x-with-pax-exam-part-i/)