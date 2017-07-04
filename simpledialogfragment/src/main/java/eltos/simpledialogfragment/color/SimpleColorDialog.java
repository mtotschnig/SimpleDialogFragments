/*
 *  Copyright 2017 Philipp Niedermayer (github.com/eltos)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eltos.simpledialogfragment.color;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import eltos.simpledialogfragment.R;
import eltos.simpledialogfragment.list.AdvancedAdapter;
import eltos.simpledialogfragment.list.CustomListDialog;


/**
 * A dialog that let's the user select a color
 *
 * Result:
 *      COLOR   int     Selected color (rgb)
 *
 * Created by eltos on 17.04.2016.
 */
public class SimpleColorDialog extends CustomListDialog<SimpleColorDialog> implements SimpleColorWheelDialog.OnDialogResultListener {


    public static final String COLOR = "simpleColorDialog.color";
    public static final int NONE = ColorView.NONE;
    protected static final int PICKER = -2;

    protected static final @ColorInt int[] DEFAULT_COLORS = new int[]{
            0xfff44336, 0xffe91e63, 0xff9c27b0, 0xff673ab7,
            0xff3f51b5, 0xff2196f3, 0xff03a9f4, 0xff00bcd4,
            0xff009688, 0xff4caf50, 0xff8bc34a, 0xffcddc39,
            0xffffeb3b, 0xffffc107, 0xffff9800, 0xffff5722,
            0xff795548, 0xff9e9e9e, 0xff607d8b
    };

    public static final @ArrayRes int MATERIAL_COLOR_PALLET = R.array.material_pallet;
    public static final @ArrayRes int MATERIAL_COLOR_PALLET_LIGHT = R.array.material_pallet_light;
    public static final @ArrayRes int MATERIAL_COLOR_PALLET_DARK = R.array.material_pallet_dark;
    public static final @ArrayRes int BEIGE_COLOR_PALLET = R.array.beige_pallet;
    public static final @ArrayRes int COLORFUL_COLOR_PALLET = R.array.colorful_pallet;


    protected static final String COLORS = "simpleColorDialog.colors";
    protected static final String CUSTOM = "simpleColorDialog.custom";
    protected static final String PICKER_DIALOG_TAG = "simpleColorDialog.picker";

    private @ColorInt int mCustomColor = NONE;
    private @ColorInt int mSelectedColor = NONE;

    public SimpleColorDialog(){
        grid();
        gridColumnWidth(R.dimen.dialog_color_item_size);
        choiceMode(SINGLE_CHOICE);
        choiceMin(1);
        colors(DEFAULT_COLORS);
    }

    public static SimpleColorDialog build(){
        return new SimpleColorDialog();
    }

    /**
     * Sets the colors to choose from
     * Default is the {@link SimpleColorDialog#DEFAULT_COLORS} set
     *
     * @param colors array of rgb-colors
     */
    public SimpleColorDialog colors(@ColorInt int[] colors){
        getArguments().putIntArray(COLORS, colors);
        return this;
    }

    /**
     * Sets the color pallet to choose from
     * May be one of {@link SimpleColorDialog#MATERIAL_COLOR_PALLET},
     * {@link SimpleColorDialog#MATERIAL_COLOR_PALLET_DARK},
     * {@link SimpleColorDialog#MATERIAL_COLOR_PALLET_LIGHT},
     * {@link SimpleColorDialog#BEIGE_COLOR_PALLET} or a custom pallet
     *
     * @param context a context to resolve the resource
     * @param colorArrayRes color array resource id
     */
    public SimpleColorDialog colors(Context context, @ArrayRes int colorArrayRes){
        return colors(context.getResources().getIntArray(colorArrayRes));
    }

    /**
     * Sets the initially selected color
     *
     * @param color the selected color
     */
    public SimpleColorDialog colorPreset(@ColorInt int color){
        getArguments().putInt(COLOR, color);
        return this;
    }

    /**
     * Set this to true to show a field with a color picker option
     * Option is ignored on Gingebread
     *
     * @param allow allow custom picked color if true
     */
    public SimpleColorDialog allowCustom(boolean allow){
        return setArg(CUSTOM, allow && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            mCustomColor = savedInstanceState.getInt(CUSTOM, mCustomColor);
        }
    }

    @Override
    protected AdvancedAdapter onCreateAdapter() {

        int[] colors = getArguments().getIntArray(COLORS);
        if (colors == null) colors = new int[0];
        boolean custom = getArguments().getBoolean(CUSTOM);

        // preset
        if (getArguments().containsKey(COLOR)){
            @ColorInt int preset = getArguments().getInt(COLOR, NONE);
            int index = indexOf(colors, preset);
            if (index < 0){ // custom preset
                mCustomColor = preset;
                if (custom){
                    choicePreset(colors.length);
                }
            } else {
                choicePreset(index);
                mSelectedColor = preset;
            }
        }

        /** Selector provided by {@link ColorView} **/
        getListView().setSelector(new ColorDrawable(Color.TRANSPARENT));

        return new ColorAdapter(colors, custom);
    }

    private int indexOf(int[] array, int item){
        for (int i = 0; i < array.length; i++) {
            if (array[i] == item){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CUSTOM, mCustomColor);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (id == PICKER){
            SimpleColorWheelDialog dialog = SimpleColorWheelDialog.build()
                    .theme(getTheme())
                    .alpha(false);
            if (mCustomColor != NONE){
                dialog.color(mCustomColor);
            } else if (mSelectedColor != NONE){
                dialog.color(mSelectedColor);
                mCustomColor = mSelectedColor;
            }
            dialog.show(this, PICKER_DIALOG_TAG);
            mSelectedColor = NONE;
        } else {
            mSelectedColor = (int) id;
        }
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (PICKER_DIALOG_TAG.equals(dialogTag) && which == BUTTON_POSITIVE){
            mCustomColor = extras.getInt(SimpleColorWheelDialog.COLOR);
            notifyDataSetChanged();
        }
        return true;
    }

    @Override
    protected Bundle onResult(int which) {
        Bundle b = super.onResult(which);
        int color = (int) b.getLong(SELECTED_SINGLE_ID);
        if (color == PICKER){
            b.putInt(COLOR, mCustomColor);
        } else {
            b.putInt(COLOR, color);
        }
        return b;
    }


    protected class ColorAdapter extends AdvancedAdapter<Integer>{

        public ColorAdapter(int[] colors, boolean addCustomField){
            if (colors == null) colors = new int[0];

            Integer[] cs = new Integer[colors.length + (addCustomField ? 1 : 0)];
            for (int i = 0; i < colors.length; i++) {
                cs[i] = colors[i];
            }
            if (addCustomField){
                cs[cs.length-1] = PICKER;
            }
            setData(cs, new ItemIdentifier<Integer>(){
                @Nullable
                public Long getIdForItem(Integer color) {
                    return Long.valueOf(color);
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ColorView item;

            if (convertView instanceof ColorView){
                item = (ColorView) convertView;
            } else {
                item = new ColorView(getContext());
            }

            int color = getItem(position);

            if ( color == PICKER){
                item.setColor(mCustomColor);
                item.setStyle(ColorView.Style.PALETTE);
            } else {
                item.setColor(getItem(position));
                item.setStyle(ColorView.Style.CHECK);
            }

            return super.getView(position, item, parent);
        }
    }

}
