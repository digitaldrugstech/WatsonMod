package eu.minemania.watson.config;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Default hotkeys configuration.
 */
public class Hotkeys
{
    public static final ConfigHotkey KEYBIND_AUTO_PAGE = new ConfigHotkey("autopage", "", "watson.hotkey.keybind_auto_page.description");
    public static final ConfigHotkey KEYBIND_COMMAND_CO_INSPECT = new ConfigHotkey("coreInspect", "", "watson.hotkey.keybind_command_co_inspect.description");
    public static final ConfigHotkey KEYBIND_CURSOR_NEXT = new ConfigHotkey("cursornext", "", "watson.hotkey.keybind_cursor_next.description");
    public static final ConfigHotkey KEYBIND_CURSOR_PREV = new ConfigHotkey("cursorprev", "", "watson.hotkey.keybind_cursor_prev.description");
    public static final ConfigHotkey KEYBIND_QUERY_AFTER = new ConfigHotkey("queryafter", "", "watson.hotkey.keybind_query_after.description");
    public static final ConfigHotkey KEYBIND_QUERY_BEFORE = new ConfigHotkey("querybefore", "", "watson.hotkey.keybind_query_before.description");
    public static final ConfigHotkey KEYBIND_SCREENSHOT = new ConfigHotkey("screenshot", "F12", "watson.hotkey.keybind_screenshot.description");
    public static final ConfigHotkey KEYBIND_TP_CURSOR = new ConfigHotkey("tpcursor", "", "watson.hotkey.keybind_tp_cursor.description");
    public static final ConfigHotkey KEYBIND_TP_NEXT = new ConfigHotkey("tpnext", "", "watson.hotkey.keybind_tp_next.description");
    public static final ConfigHotkey KEYBIND_TP_NEXT_ANNO = new ConfigHotkey("tpnextanno", "", "watson.hotkey.keybind_tp_next_anno.description");
    public static final ConfigHotkey KEYBIND_TP_PREV = new ConfigHotkey("tpprev", "", "watson.hotkey.keybind_tp_prev.description");
    public static final ConfigHotkey KEYBIND_TP_PREV_ANNO = new ConfigHotkey("tpprevanno", "", "watson.hotkey.keybind_tp_prev_anno.description");
    public static final ConfigHotkey KEYBIND_WATSON_CLEAR = new ConfigHotkey("watsonClear", "", "watson.hotkey.keybind_watson_clear.description");
    public static final ConfigHotkey OPEN_GUI_MAIN_MENU = new ConfigHotkey("openGuiMainMenu", "L", KeybindSettings.RELEASE_EXCLUSIVE, "watson.hotkey.open_gui_main_menu.description");
    public static final ConfigHotkey OPEN_GUI_SETTINGS = new ConfigHotkey("openGuiSettings", "L,C", "watson.hotkey.open_gui_settings.description");

    public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
            KEYBIND_AUTO_PAGE,
            KEYBIND_COMMAND_CO_INSPECT,
            KEYBIND_CURSOR_NEXT,
            KEYBIND_CURSOR_PREV,
            KEYBIND_QUERY_AFTER,
            KEYBIND_QUERY_BEFORE,
            KEYBIND_SCREENSHOT,
            KEYBIND_TP_CURSOR,
            KEYBIND_TP_NEXT,
            KEYBIND_TP_NEXT_ANNO,
            KEYBIND_TP_PREV,
            KEYBIND_TP_PREV_ANNO,
            KEYBIND_WATSON_CLEAR,
            OPEN_GUI_MAIN_MENU,
            OPEN_GUI_SETTINGS
    );
}