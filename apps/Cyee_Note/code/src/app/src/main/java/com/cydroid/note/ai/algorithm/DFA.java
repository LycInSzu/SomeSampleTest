package com.cydroid.note.ai.algorithm;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaojt on 16-1-28.
 */
public class DFA {

    private TreeNode mRootNode = new TreeNode();

    private String mCharset = "GBK";

    private ByteBuffer mKeywordBuffer = ByteBuffer.allocate(128);

    public void createKeywordTree(List<String> keywordList) throws UnsupportedEncodingException {

        for (String keyword : keywordList) {
            if (keyword == null) continue;
            keyword = keyword.trim();
            addKeyword(keyword);
        }
    }

    public void addKeyword(String keyword) throws UnsupportedEncodingException {
        byte[] bytes = keyword.getBytes(mCharset);

        TreeNode tempNode = mRootNode;
        //循环每个字节
        for (int i = 0; i < bytes.length; i++) {
            int index = bytes[i] & 0xff; //字符转换成数字
            TreeNode node = tempNode.getSubNode(index);

            if (node == null) { //没初始化
                node = new TreeNode();
                tempNode.setSubNode(index, node);
            }

            tempNode = node;

            if (i == bytes.length - 1) {
                tempNode.setKeywordEnd(true);    //关键词结束， 设置结束标志
            }
        }
    }

    public ArrayList<String> searchKeyword(String text) throws UnsupportedEncodingException {
        ArrayList<String> searchKeyWords = searchKeyword(text.getBytes(mCharset));
        return merge(searchKeyWords);
    }

    private ArrayList<String> merge(ArrayList<String> keywords) {
        ArrayList<String> target = new ArrayList<>();
        for (String k : keywords) {
            if (!isExist(target, k)) {
                target.add(k);
            }
        }

        return target;
    }

    private boolean isExist(ArrayList<String> list, String key) {
        for (String item : list) {
            if (key.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> searchKeyword(byte[] bytes) {
        ArrayList words = new ArrayList();
        if (bytes == null || bytes.length == 0) {
            return words;
        }

        TreeNode tempNode = mRootNode;
        int rollback = 0;   //回滚数
        int position = 0; //当前比较的位置
        ByteBuffer keywordBuffer = mKeywordBuffer;
        keywordBuffer.clear();

        while (position < bytes.length) {
            int index = bytes[position] & 0xFF;
            keywordBuffer.put(bytes[position]); //写关键词缓存
            tempNode = tempNode.getSubNode(index);

            //当前位置的匹配结束
            if (tempNode == null) {
                position = position - rollback; //回退 并测试下一个字节
                rollback = 0;
                tempNode = mRootNode;    //状态机复位
                keywordBuffer.clear();  //清空
            } else if (tempNode.isKeywordEnd()) {  //是结束点 记录关键词
                keywordBuffer.flip();
                String keyword = Charset.forName(mCharset).decode(keywordBuffer).toString();

                keywordBuffer.limit(keywordBuffer.capacity());
                words.add(keyword);

                rollback = 1;   //遇到结束点  rollback 置为1
            } else {
                rollback++; //非结束点 回退数加1
            }

            position++;
        }

        return words;
    }

    public void setmCharset(String mCharset) {
        this.mCharset = mCharset;
    }
}
