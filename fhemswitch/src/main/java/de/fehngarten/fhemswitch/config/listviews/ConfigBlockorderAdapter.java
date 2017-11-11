package de.fehngarten.fhemswitch.config.listviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.fehngarten.fhemswitch.R;

import static de.fehngarten.fhemswitch.global.Settings.settingBlockNames;

class ConfigBlockorderAdapter extends ConfigAdapter {
    private Context mContext;
    private String[] blockorderRows;

    ConfigBlockorderAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void initData(String[] blocks) {
        blockorderRows = blocks;
        notifyDataSetChanged();
    }

    public String[] getData() {
        return blockorderRows;
    }

    public int getCount() {
        if (blockorderRows == null) {
            return 0;
        } else {
            return blockorderRows.length;
        }
    }

    public String getItem(int position) {
        return blockorderRows[position];
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        String block = getItem(position);
        final blockorderHolder blockorderHolder;

        if (rowView == null) {
            blockorderHolder = new blockorderHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.config_row_blockorder, parent, false);
            rowView.setTag(blockorderHolder);
            blockorderHolder.block_name = (TextView) rowView.findViewById(R.id.config_block_name);
            blockorderHolder.block_index = (TextView) rowView.findViewById(R.id.config_block_index);
        } else {
            blockorderHolder = (blockorderHolder) rowView.getTag();
        }

        //blockorderHolder.ref = position;
        blockorderHolder.block_name.setText(settingBlockNames.get(block));
        blockorderHolder.block_index.setText(block);
        return rowView;
    }

    public void changeItems(int from, int to) {
        final String[] blockorderRowsTemp = new String[blockorderRows.length];
        if (from > to) {
            for (int i = 0; i < blockorderRows.length; i++) {
                if (i < to) {
                    blockorderRowsTemp[i] = blockorderRows[i];
                } else if (i == to) {
                    blockorderRowsTemp[i] = blockorderRows[from];
                } else if (i <= from) {
                    blockorderRowsTemp[i] = blockorderRows[i - 1];
                } else {
                    blockorderRowsTemp[i] = blockorderRows[i];
                }
            }
        } else if (from < to) {
            for (int i = 0; i < blockorderRows.length; i++) {
                if (i < from) {
                    blockorderRowsTemp[i] = blockorderRows[i];
                } else if (i < to) {
                    blockorderRowsTemp[i] = blockorderRows[i + 1];
                } else if (i == to) {
                    blockorderRowsTemp[i] = blockorderRows[from];
                } else {
                    blockorderRowsTemp[i] = blockorderRows[i];
                }
            }
        }
        if (from != to) {
            blockorderRows = blockorderRowsTemp;
            notifyDataSetChanged();
        }
    }

    private class blockorderHolder {
        TextView block_name;
        TextView block_index;
        //int ref;
    }
}
