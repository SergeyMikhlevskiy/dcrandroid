package com.dcrandroid.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dcrandroid.R;

import java.util.ArrayList;
import java.util.List;

public class ListViewItemAdapter extends ArrayAdapter<ListViewItemAdapter.TransactionInfoItem> {


    private Context mContext;
    private List<TransactionInfoItem> items;
    private TextView tvInfo;

    public ListViewItemAdapter(@NonNull Context context, ArrayList<TransactionInfoItem> list) {
        super(context, 0, list);
        mContext = context;
        items = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item_adapter,parent,false);
        TransactionInfoItem transactionInfoItem = items.get(position);

        TextView tvAmount = listItem.findViewById(R.id.tvAmount);
        tvAmount.setText(transactionInfoItem.getAmount());

        tvInfo = listItem.findViewById(R.id.tvInfo);
        tvInfo.setText(transactionInfoItem.getInfo());

        return listItem;
    }


    public TextView getTvInfo() {
        return tvInfo;
    }

    public static class TransactionInfoItem {
        private String amount;
        private String info;

        public TransactionInfoItem(String amount, String info) {
            this.amount = amount;
            this.info = info;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

}
