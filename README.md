NOTE
----
Since WSO2 ESB Analytics will be included in the WSO2 Integrator(http://wso2.com/integration) as the Analytics profile, we are deprecating this product and moving it to the attic.

# WSO2 ESB Analytics
WSO2 ESB Analytics provides capabilities to monitor the services which are deployed in the WSO2 ESB. It provides the ability to monitor statistics of top level components like SOAP services (Proxy), REST services (API) and second level components like inbound endpoints, sequences, endpoints and the atomic level components of mediators. WSO2 ESB analytics is powered by the WSO2 Data Analytics Server (DAS).

# WSO2 ESB Analytics has the following key capabilities

- Monitoring coarse grained statistics like Transactions Per Second (TPS), request count, failure count, latency (min, max, average) for ESB artifacts like Proxy, API, Sequence, Endpoint, Inbound endpoint
- Monitoring fine grained, mediator level statistics for a given message flow or for a given message
- Tracing the messages which are passing through the WSO2 ESB. Users can choose upto which level they want to trace messages (e.g. trace message payload only)
- Pre built dashboard to navigate through different artifacts while tracing a message so that users can take the context across different graphs
- Ability to scale at different layers according to the load of the system (receivers, analyzers, databases)

# How to install
You can install the WSO2 ESB analytics component by following the ESB [documentation](https://docs.wso2.com/display/ESB500/WSO2+ESB+Analytics)

# How to Contribute

* Please report issues to our [ESB Analytics JIRA](https://wso2.org/jira/browse/ANLYESB)
* Send your pull requests to the [analytics-esb](https://github.com/wso2/analytics-esb) repository

# Contact us

You can talk to us via the following mailing lists:

* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org
