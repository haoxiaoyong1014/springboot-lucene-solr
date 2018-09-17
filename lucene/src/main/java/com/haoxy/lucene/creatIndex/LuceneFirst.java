package com.haoxy.lucene.creatIndex;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;

/**
 * Created by haoxy on 2018/9/17.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 */
//创建索引
public class LuceneFirst {
    /**
     * 1, 指定索引库的存放位置,可以是内存也可以是磁盘, 存放在内存一般不用
     * 2, 创建一个 indexWriter 对象 需要一个分析器对象
     * 3, 获取原始文档 需要 IO流 读取文本文件
     * 4, 创建文档对象
     * 5, 向文档中添加域
     * 6, 将文档对象写入索引库
     * 7,关闭 indexWriter
     */
    public static void creatIndex() throws IOException {

        // 1, 指定索引库的存放位置,可以是内存也可以是磁盘, 存放在内存一般不用
        //Directory directory=new RAMDirectory();
        //保存在磁盘中
        Directory directory = FSDirectory.open(new File("tmp/index"));
        //2, 创建一个 indexWriter 对象 需要一个分析器对象
        Analyzer analyzer = new StandardAnalyzer();
        //参数1:lucene版本号,参数2:分析器对象
        IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
        //参数1: 索引库存放路径, 参数2: 配置信息,其中包含分析器对象
        IndexWriter indexWriter = new IndexWriter(directory, config);
        //3, 获取原始文档 需要 IO流 读取文本文件
        File docPath = new File("/tmp/searchsource");
        for (File file : docPath.listFiles()) {
            //获取文件名
            String fileName = file.getName();
            //获取文件路径
            String filePath = file.getPath();
            //获取文件大小
            long fileSize = FileUtils.sizeOf(file);
            //获取文件内容
            String fileContent = FileUtils.readFileToString(file);
            //4, 创建文档对象
            Document document = new Document();
            //创建域
            //参数1: 域名称,参数2: 域的内容 参数3: 是否存储
            TextField textField = new TextField("name", fileName, Field.Store.YES);
            StoredField storedField = new StoredField("path", filePath);
            TextField textField1 = new TextField("content", fileContent, Field.Store.NO);
            LongField longField = new LongField("size", fileSize, Field.Store.YES);
            //5, 向文档中添加域
            document.add(textField);
            document.add(storedField);
            document.add(textField1);
            document.add(longField);
            //6、把文档对象写入索引库
            indexWriter.addDocument(document);
        }
        //7、关闭IndexWriter对象
        indexWriter.close();
    }

    public static void main(String[] args) throws IOException {
        LuceneFirst.creatIndex();
    }

}
