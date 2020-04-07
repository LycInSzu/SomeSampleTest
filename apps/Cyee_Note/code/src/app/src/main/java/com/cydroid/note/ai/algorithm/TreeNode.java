package com.cydroid.note.ai.algorithm;

import android.util.SparseArray;

/**
 * Created by gaojt on 16-1-28.
 */
public class TreeNode {

    private boolean end = false;

    private SparseArray<TreeNode> subNodes = new SparseArray<>();

    void setSubNode(int index, TreeNode node) {
        subNodes.put(index, node);
    }

    TreeNode getSubNode(int index) {
        return subNodes.get(index);
    }


    boolean isKeywordEnd() {
        return end;
    }

    void setKeywordEnd(boolean end) {
        this.end = end;
    }
}
