
package com.open_demo.util;

public class Config {
    /** 对话框样式 */

    /**
     * 当前识别语言
     */

    private static int CURRENT_LANGUAGE_INDEX = 0;
    
    /**
     * 当前垂直领域类型
     */
    
    private static int CURRENT_PROP_INDEX = 0;

  

    public static int getCurrentLanguageIndex() {
        return CURRENT_LANGUAGE_INDEX;
    }

        
    public static int getCurrentPropIndex() {
        return CURRENT_PROP_INDEX;
    }

    /**
     * 播放开始音
     */
    public static boolean PLAY_START_SOUND = true;

    /**
     * 播放结束音
     */
    public static boolean PLAY_END_SOUND = true;
    
    /**
     * 对话框提示音
     */
    public static boolean DIALOG_TIPS_SOUND = true;

    /**
     * 显示音量
     */
    public static boolean SHOW_VOL = true;

}
