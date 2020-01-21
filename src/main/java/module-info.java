/**
 * @author Grégory Van den Borre
 */
open module be.yildizgames.module.webserver.undertow {

    requires undertow.core;
    requires com.fasterxml.jackson.databind;

    exports be.yildizgames.module.webserver.undertow;
}
