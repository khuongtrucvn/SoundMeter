package com.app.khoaluan.noizy.utils;

import android.content.Context;
import android.util.Xml;
import android.widget.Toast;

import com.app.khoaluan.noizy.R;
import com.app.khoaluan.noizy.model.MeasureResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class UtilsXmlFile {
    private static final String FILE_NAME = "history.xml";

    public UtilsXmlFile() { }

    public void writeXmlFile(Context context, List<MeasureResult> resultList){
        File historyFile = new File(context.getFilesDir() + "/" + FILE_NAME);

        if(historyFile.exists()){
            //Đọc và lưu lịch sử cũ vào list
            List<MeasureResult> oldList = readXmlFile(context);
            //Xoá file xml cũ
            deleteXmlFile(context);
            //Thêm tất cả lịch sử của list cũ vào list mới
            resultList.addAll(oldList);
        }

        if(writeXmlFormat(context,resultList)){
            Toast.makeText(context, R.string.noti_history_saved, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context, R.string.noti_history_unsaved, Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức lấy giá trị của node
    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    public List<MeasureResult> readXmlFile(Context context) {
        List<MeasureResult> resultList = new ArrayList<>();

        File historyFile = new File(context.getFilesDir() + "/" + FILE_NAME);

        if(historyFile.exists()){
            FileInputStream fis;

            try{
                fis = context.openFileInput(FILE_NAME);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fis);

                Element element=doc.getDocumentElement();
                element.normalize();

                NodeList nList = doc.getElementsByTagName("Item");
                for (int i=0; i<nList.getLength(); i++) {

                    Node node = nList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element2 = (Element) node;

                        float curValue = Float.parseFloat(getValue("CurValue", element2));
                        float minValue = Float.parseFloat(getValue("MinValue", element2));
                        float avgValue = Float.parseFloat(getValue("AvgValue", element2));
                        float maxValue = Float.parseFloat(getValue("MaxValue", element2));
                        String date = getValue("Date", element2);
                        String time = getValue("Time", element2);
                        String duration = getValue("Duration", element2);

                        MeasureResult result = new MeasureResult(curValue,minValue,avgValue,maxValue,date,time,duration);
                        resultList.add(result);
                    }
                }

                fis.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        return resultList;
    }

    //phương thức lấy list sau khi đã xoá node
    public List<MeasureResult> getNodeDeleteList(Context context, int id){
        //Đọc và lưu lịch sử cũ vào list
        List<MeasureResult> resultList = readXmlFile(context);
        resultList.remove(id);

        return resultList;
    }

    public void deleteNodeXmlFile(Context context, List<MeasureResult> list){
        //Xoá file xml cũ
        deleteXmlFile(context);

        if(writeXmlFormat(context,list)){
            Toast.makeText(context, R.string.noti_history_delete, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context, R.string.noti_history_delete_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteXmlFile(Context context){
        File historyFile = new File(context.getFilesDir() + "/" + FILE_NAME);

        if(historyFile.exists()){
            historyFile.delete();
        }
    }

    private boolean writeXmlFormat(Context context, List<MeasureResult> list){
        boolean result;

        FileOutputStream fos;
        try{
            fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);

            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null,true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, "History");
            for(MeasureResult ms : list){
                serializer.startTag(null, "Item");

                serializer.startTag(null, "CurValue");
                serializer.text(Float.toString(ms.getCurValue()));
                serializer.endTag(null, "CurValue");

                serializer.startTag(null, "MinValue");
                serializer.text(Float.toString(ms.getMinValue()));
                serializer.endTag(null, "MinValue");

                serializer.startTag(null, "AvgValue");
                serializer.text(Float.toString(ms.getAvgValue()));
                serializer.endTag(null, "AvgValue");

                serializer.startTag(null, "MaxValue");
                serializer.text(Float.toString(ms.getMaxValue()));
                serializer.endTag(null, "MaxValue");

                serializer.startTag(null, "Date");
                serializer.text(ms.getDate());
                serializer.endTag(null, "Date");

                serializer.startTag(null, "Time");
                serializer.text(ms.getTime());
                serializer.endTag(null, "Time");

                serializer.startTag(null, "Duration");
                serializer.text(ms.getDuration());
                serializer.endTag(null, "Duration");

                serializer.endTag(null, "Item");
            }
            serializer.endTag(null, "History");

            serializer.endDocument();
            serializer.flush();

            fos.close();
            result = true;
        }
        catch (Exception e){
            e.printStackTrace();
            result = false;
        }

        return result;
    }
}
