package com.gemini.jalen.ligament.widget.list;

import java.util.List;

public interface DataHolder {
    void addItem(Object obj);

    void addItem(List<Object> list);

    void clear();

    void moveItem(int fromPosition, int toPosition);
}
