/**
 * @author Grégory Van den Borre
 */
open module be.yildizgames.module.webserver.undertow {

    requires undertow.core;
    requires com.fasterxml.jackson.databind;
    requires jdk.unsupported;
    requires com.fasterxml.jackson.dataformat.yaml;

    exports be.yildizgames.module.webserver;
}
