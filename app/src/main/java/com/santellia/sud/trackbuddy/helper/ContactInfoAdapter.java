package com.santellia.sud.trackbuddy.helper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.santellia.sud.trackbuddy.R;
import com.santellia.sud.trackbuddy.model.ContactInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sudarshan on 18/03/16.
 */
public class ContactInfoAdapter extends BaseAdapter {

    public List<ContactInfo> _data;
    private ArrayList<ContactInfo> arrayList;
    Context mContext;
    Holder h;

    public ContactInfoAdapter (List<ContactInfo> contactInfos, Context context) {
        _data = contactInfos;
        mContext = context;
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(_data);
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public Object getItem(int i) {
        return _data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;

        if (view == null) {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.item_contact, null);
            Log.e("Inside", "In view1");
        } else {
            view = convertView;
            Log.e("Inside", "In view2");
        }

        h = new Holder();

        h.title = (TextView) view.findViewById(R.id.tvContactName);
        h.phone = (TextView) view.findViewById(R.id.tvContactNumber);

        final ContactInfo data = _data.get(i);
        h.title.setText(data.getContactName());
        h.phone.setText(data.getContactNumber());

        view.setTag(data);
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        _data.clear();
        if (charText.length() == 0) {
            _data.addAll(arrayList);
        } else {
            for (ContactInfo ci : arrayList) {
                if (ci.getContactName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    _data.add(ci);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class Holder {
        TextView title, phone;
    }
}
