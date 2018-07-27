package org.apache.batik.css.engine.value;
import org.apache.batik.util.CSSConstants;
import org.w3c.dom.css.CSSPrimitiveValue;
public interface ValueConstants {
    Value NUMBER_0 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 0);
    Value NUMBER_100 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 100);
    Value NUMBER_128 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 128);
    Value NUMBER_192 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 192);
    Value NUMBER_200 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 200);
    Value NUMBER_255 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 255);
    Value NUMBER_300 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 300);
    Value NUMBER_400 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 400);
    Value NUMBER_500 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 500);
    Value NUMBER_600 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 600);
    Value NUMBER_700 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 700);
    Value NUMBER_800 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 800);
    Value NUMBER_900 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 900);
    Value INHERIT_VALUE = InheritValue.INSTANCE;
    Value ALL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_ALL_VALUE);
    Value AUTO_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_AUTO_VALUE);
    Value BIDI_OVERRIDE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BIDI_OVERRIDE_VALUE);
    Value BLINK_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BLINK_VALUE);
    Value BLOCK_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BLOCK_VALUE);
    Value BOLD_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BOLD_VALUE);
    Value BOLDER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BOLDER_VALUE);
    Value BOTTOM_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BOTTOM_VALUE);
    Value COLLAPSE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_COLLAPSE_VALUE);
    Value COMPACT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_COMPACT_VALUE);
    Value CONDENSED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_CONDENSED_VALUE);
    Value CRISPEDGES_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_CRISPEDGES_VALUE);
    Value CROSSHAIR_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_CROSSHAIR_VALUE);
    Value CURSIVE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_CURSIVE_VALUE);
    Value DEFAULT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_DEFAULT_VALUE);
    Value E_RESIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_E_RESIZE_VALUE);
    Value EMBED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_EMBED_VALUE);
    Value EXPANDED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_EXPANDED_VALUE);
    Value EXTRA_CONDENSED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_EXTRA_CONDENSED_VALUE);
    Value EXTRA_EXPANDED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_EXTRA_EXPANDED_VALUE);
    Value FANTASY_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_FANTASY_VALUE);
    Value HELP_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_HELP_VALUE);
    Value HIDDEN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_HIDDEN_VALUE);
    Value INLINE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_INLINE_VALUE);
    Value INLINE_TABLE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_INLINE_TABLE_VALUE);
    Value ITALIC_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_ITALIC_VALUE);
    Value LARGE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_LARGE_VALUE);
    Value LARGER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_LARGER_VALUE);
    Value LIGHTER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_LIGHTER_VALUE);
    Value LINE_THROUGH_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_LINE_THROUGH_VALUE);
    Value LIST_ITEM_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_LIST_ITEM_VALUE);
    Value LTR_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_LTR_VALUE);
    Value MARKER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_MARKER_VALUE);
    Value MEDIUM_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_MEDIUM_VALUE);
    Value MONOSPACE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_MONOSPACE_VALUE);
    Value MOVE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_MOVE_VALUE);
    Value N_RESIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_N_RESIZE_VALUE);
    Value NARROWER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_NARROWER_VALUE);
    Value NE_RESIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_NE_RESIZE_VALUE);
    Value NW_RESIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_NW_RESIZE_VALUE);
    Value NONE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_NONE_VALUE);
    Value NORMAL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_NORMAL_VALUE);
    Value OBLIQUE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_OBLIQUE_VALUE);
    Value OVERLINE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_OVERLINE_VALUE);
    Value POINTER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_POINTER_VALUE);
    Value PAINTED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_PAINTED_VALUE);
    Value RTL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_RTL_VALUE);
    Value RUN_IN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_RUN_IN_VALUE);
    Value S_RESIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_S_RESIZE_VALUE);
    Value SANS_SERIF_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SANS_SERIF_VALUE);
    Value SCROLL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SCROLL_VALUE);
    Value SE_RESIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SE_RESIZE_VALUE);
    Value SEMI_CONDENSED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SEMI_CONDENSED_VALUE);
    Value SEMI_EXPANDED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SEMI_EXPANDED_VALUE);
    Value SERIF_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SERIF_VALUE);
    Value SMALL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SMALL_VALUE);
    Value SMALL_CAPS_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SMALL_CAPS_VALUE);
    Value SMALLER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SMALLER_VALUE);
    Value STROKE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_STROKE_VALUE);
    Value SW_RESIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SW_RESIZE_VALUE);
    Value TABLE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_VALUE);
    Value TABLE_CAPTION_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_CAPTION_VALUE);
    Value TABLE_CELL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_CELL_VALUE);
    Value TABLE_COLUMN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_COLUMN_VALUE);
    Value TABLE_COLUMN_GROUP_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_COLUMN_GROUP_VALUE);
    Value TABLE_FOOTER_GROUP_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_FOOTER_GROUP_VALUE);
    Value TABLE_HEADER_GROUP_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_HEADER_GROUP_VALUE);
    Value TABLE_ROW_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_ROW_VALUE);
    Value TABLE_ROW_GROUP_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TABLE_ROW_GROUP_VALUE);
    Value TEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TEXT_VALUE);
    Value ULTRA_CONDENSED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_ULTRA_CONDENSED_VALUE);
    Value ULTRA_EXPANDED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_ULTRA_EXPANDED_VALUE);
    Value TOP_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TOP_VALUE);
    Value UNDERLINE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_UNDERLINE_VALUE);
    Value VISIBLE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_VISIBLE_VALUE);
    Value W_RESIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_W_RESIZE_VALUE);
    Value WAIT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_WAIT_VALUE);
    Value WIDER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_WIDER_VALUE);
    Value X_LARGE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_X_LARGE_VALUE);
    Value X_SMALL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_X_SMALL_VALUE);
    Value XX_LARGE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_XX_LARGE_VALUE);
    Value XX_SMALL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_XX_SMALL_VALUE);
    Value AQUA_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_AQUA_VALUE);
    Value BLACK_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BLACK_VALUE);
    Value BLUE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BLUE_VALUE);
    Value FUCHSIA_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_FUCHSIA_VALUE);
    Value GRAY_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_GRAY_VALUE);
    Value GREEN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_GREEN_VALUE);
    Value LIME_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_LIME_VALUE);
    Value MAROON_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_MAROON_VALUE);
    Value NAVY_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_NAVY_VALUE);
    Value OLIVE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_OLIVE_VALUE);
    Value PURPLE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_PURPLE_VALUE);
    Value RED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_RED_VALUE);
    Value SILVER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SILVER_VALUE);
    Value TEAL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_TEAL_VALUE);
    Value WHITE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_WHITE_VALUE);
    Value YELLOW_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_YELLOW_VALUE);
    Value ACTIVEBORDER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_ACTIVEBORDER_VALUE);
    Value ACTIVECAPTION_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_ACTIVECAPTION_VALUE);
    Value APPWORKSPACE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_APPWORKSPACE_VALUE);
    Value BACKGROUND_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BACKGROUND_VALUE);
    Value BUTTONFACE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BUTTONFACE_VALUE);
    Value BUTTONHIGHLIGHT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BUTTONHIGHLIGHT_VALUE);
    Value BUTTONSHADOW_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BUTTONSHADOW_VALUE);
    Value BUTTONTEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_BUTTONTEXT_VALUE);
    Value CAPTIONTEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_CAPTIONTEXT_VALUE);
    Value GRAYTEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_GRAYTEXT_VALUE);
    Value HIGHLIGHT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_HIGHLIGHT_VALUE);
    Value HIGHLIGHTTEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_HIGHLIGHTTEXT_VALUE);
    Value INACTIVEBORDER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_INACTIVEBORDER_VALUE);
    Value INACTIVECAPTION_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_INACTIVECAPTION_VALUE);
    Value INACTIVECAPTIONTEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_INACTIVECAPTIONTEXT_VALUE);
    Value INFOBACKGROUND_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_INFOBACKGROUND_VALUE);
    Value INFOTEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_INFOTEXT_VALUE);
    Value MENU_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_MENU_VALUE);
    Value MENUTEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_MENUTEXT_VALUE);
    Value SCROLLBAR_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_SCROLLBAR_VALUE);
    Value THREEDDARKSHADOW_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_THREEDDARKSHADOW_VALUE);
    Value THREEDFACE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_THREEDFACE_VALUE);
    Value THREEDHIGHLIGHT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_THREEDHIGHLIGHT_VALUE);
    Value THREEDLIGHTSHADOW_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_THREEDLIGHTSHADOW_VALUE);
    Value THREEDSHADOW_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_THREEDSHADOW_VALUE);
    Value WINDOW_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_WINDOW_VALUE);
    Value WINDOWFRAME_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_WINDOWFRAME_VALUE);
    Value WINDOWTEXT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CSSConstants.CSS_WINDOWTEXT_VALUE);
    Value BLACK_RGB_VALUE =
        new RGBColorValue(NUMBER_0, NUMBER_0, NUMBER_0);
    Value SILVER_RGB_VALUE =
        new RGBColorValue(NUMBER_192, NUMBER_192, NUMBER_192);
    Value GRAY_RGB_VALUE =
        new RGBColorValue(NUMBER_128, NUMBER_128, NUMBER_128);
    Value WHITE_RGB_VALUE =
        new RGBColorValue(NUMBER_255, NUMBER_255, NUMBER_255);
    Value MAROON_RGB_VALUE =
        new RGBColorValue(NUMBER_128, NUMBER_0, NUMBER_0);
    Value RED_RGB_VALUE =
        new RGBColorValue(NUMBER_255, NUMBER_0, NUMBER_0);
    Value PURPLE_RGB_VALUE =
        new RGBColorValue(NUMBER_128, NUMBER_0, NUMBER_128);
    Value FUCHSIA_RGB_VALUE =
        new RGBColorValue(NUMBER_255, NUMBER_0, NUMBER_255);
    Value GREEN_RGB_VALUE =
        new RGBColorValue(NUMBER_0, NUMBER_128, NUMBER_0);
    Value LIME_RGB_VALUE =
        new RGBColorValue(NUMBER_0, NUMBER_255, NUMBER_0);
    Value OLIVE_RGB_VALUE =
        new RGBColorValue(NUMBER_128, NUMBER_128, NUMBER_0);
    Value YELLOW_RGB_VALUE =
        new RGBColorValue(NUMBER_255, NUMBER_255, NUMBER_0);
    Value NAVY_RGB_VALUE =
        new RGBColorValue(NUMBER_0, NUMBER_0, NUMBER_128);
    Value BLUE_RGB_VALUE =
        new RGBColorValue(NUMBER_0, NUMBER_0, NUMBER_255);
    Value TEAL_RGB_VALUE =
        new RGBColorValue(NUMBER_0, NUMBER_128, NUMBER_128);
    Value AQUA_RGB_VALUE =
        new RGBColorValue(NUMBER_0, NUMBER_255, NUMBER_255);
}
