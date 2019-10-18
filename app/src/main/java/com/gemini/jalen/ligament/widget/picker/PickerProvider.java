package com.gemini.jalen.ligament.widget.picker;

import java.util.List;

public interface PickerProvider {
    void loadItem(PickerBean bean, Sender sender);

    interface Sender {
        void send(List<PickerBean> list);
    }
}
