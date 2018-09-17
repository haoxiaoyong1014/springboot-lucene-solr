package com.haoxy.lucene.creatIndex;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;

/**
 * Created by haoxy on 2018/9/17.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 */
public class IndexManageer {

    public IndexWriter getIndexWriter() throws Exception {
        Directory directory = FSDirectory.open(new File("/tmp/index"));
        Analyzer analyzer = new IKAnalyzer();
        //参数1：lucene的版本号，第二个参数：分析器对象
        IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, conf);
        return indexWriter;
    }

    //向索引库中添加文档
    public void addDocument() throws Exception {
        Directory directory = FSDirectory.open(new File("/tmp/index"));
        Analyzer analyzer = new IKAnalyzer();
        //参数1：lucene的版本号，第二个参数：分析器对象
        IndexWriterConfig conf = new IndexWriterConfig(Version.LATEST, analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, conf);
        //创建Document对象
        Document document = new Document();
        //创建域
        TextField FileNameField = new TextField("name", "测试文件.txt", Field.Store.YES);
        StoredField FilepathField = new StoredField("path", "/tmp/测试文件.txt");
        document.add(FileNameField);
        document.add(FilepathField);
        //写入索引库
        indexWriter.addDocument(document);
        //关闭资源
        indexWriter.close();

    }

    //删除全部文档

    public void deleteAllDocument() throws Exception {
        //获得IndexWriter对象
        IndexWriter indexWriter = this.getIndexWriter();
        //调用删除方法删除索引库
        indexWriter.deleteAll();
        //关闭资源
        indexWriter.close();

    }


    public void deleteDocumentByQuery() throws Exception {
        IndexWriter indexWriter = this.getIndexWriter();
        //指定查询条件
        Query query = new TermQuery(new Term("name", "apache"));
        //删除文档
        indexWriter.deleteDocuments(query);
        //关闭资源
        indexWriter.close();
    }

    //更新索引库

    public void updateDocument() throws Exception {
        IndexWriter indexWriter = this.getIndexWriter();
        //创建一个新的文档对象
        Document document = new Document();
        document.add(new TextField("name", "更新后的文档", Field.Store.YES));
        document.add(new TextField("content", "更新后的文档内容", Field.Store.YES));
        //term对象：指定要删除域及要删除的关键词，先根据term查询，把查询结果删除，然后追加一个新的文档。
        indexWriter.updateDocument(new Term("name", "spring"), document);
        //关闭资源
        indexWriter.close();
    }
}
