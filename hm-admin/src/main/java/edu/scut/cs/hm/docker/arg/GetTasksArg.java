package edu.scut.cs.hm.docker.arg;

/**
 * Planned for 'filters':
 * <pre>
 * filters – a JSON encoded value of the filters (a map[string][]string) to process on the services list. Available filters:
 id=<task id>
 name=<task name>
 service=<service name>
 node=<node id or name>
 label=key or label="key=value"
 desired-state=(running | shutdown | accepted)
 * </pre>
 */
public class GetTasksArg {
}
