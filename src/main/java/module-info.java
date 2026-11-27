/**
 * Module for the undertow web server.
 * @author Grégory Van den Borre
 */
open module be.yildizgames.module.webserver.undertow {

    requires undertow.core;
    requires be.yildizgames.module.serialization;

    exports be.yildizgames.module.webserver;
}
