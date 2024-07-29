/**
 * Module for the undertow web server.
 * @author Grégory Van den Borre
 */
open module be.yildizgames.module.webserver.undertow {

    requires undertow.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires jdk.unsupported;

    exports be.yildizgames.module.webserver;
}
