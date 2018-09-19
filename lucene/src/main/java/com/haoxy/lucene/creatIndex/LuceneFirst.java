package com.haoxy.lucene.creatIndex;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wltea.analyzer.lucene.IKAnalyzer;

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
     * 创建索引
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
        Directory directory = FSDirectory.open(new File("/tmp/index"));
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
        //LuceneFirst.creatIndex();
        //LuceneFirst.searchIndex();
        LuceneFirst.testAnanlyzer();
    }

    /**
     * 查询索引
     * 1, 指定索引库存储的位置
     * 2,使用IndexReade 对象打开索引库
     * 3,创建一个IndexSearcher 对象 ,构造方法中需要 IndexReade 参数
     * 4, 创建一个查询对象,需要指定查询域和查询条件
     * 5,取出查询结果
     * 6 遍历查询结果并打印.
     * 7 关闭 IndexReade
     */
    public static void searchIndex() throws IOException {

        //1, 指定索引库存储的位置
        Directory directory = FSDirectory.open(new File("/tmp/index/"));
        //2,使用IndexReade 对象打开索引库
        IndexReader indexReader = DirectoryReader.open(directory);
        //3,创建一个IndexSearcher 对象 ,构造方法中需要 IndexReade 参数
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //4, 创建一个查询对象,需要指定查询域和查询条件
        //term的参数1：要搜索的域 参数2：搜索的关键字
        Query query = new TermQuery(new Term("name", "apache"));
        //参数1：查询条件 参数2：查询结果返回的最大值
        // 5,取出查询结果
        TopDocs topDocs = indexSearcher.search(query, 10);
        //取查询结果总记录数
        System.out.println("查询结果总记录数：" + topDocs.totalHits);
        //6遍历查询结果并打印.
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //取文档 id
            int doc = scoreDoc.doc;
            //从索引库去文档对象
            Document document = indexSearcher.doc(doc);
            System.out.println("文件名: " + document.get("name"));
            System.out.println("文件内容: " + document.get("content"));//因为没有存(Field.Store.NO),所以查询不出来
            System.out.println("文件大小: " + document.get("size"));
            System.out.println("文件路径: " + document.get("path"));
        }
        //关闭 IndexReade
        indexReader.close();
    }


    /**
     * 查看分析器的分词效果
     * 1, 创建一个分析器对象
     * 2, 从分析器中获的 tokenStream对象
     * 3, 设置一个引用，引用可以有多重类型，可以是关键词的引用、偏移量的引用
     * 4, 调用tokenStream的reset方法
     * 5,使用while循环变量单词列表
     */
    public static void testAnanlyzer() throws IOException {
        //1, 创建一个分析器对象
        //Analyzer analyzer = new StandardAnalyzer();
        //Analyzer analyzer = new CJKAnalyzer();
		//Analyzer analyzer = new SmartChineseAnalyzer(); //智能中文分词器
        Analyzer analyzer = new IKAnalyzer();//IK分词器
        //2,从分析器中获的 tokenStream对象
        //参数1：域的名称，可以为null或者""
        //参数2：要分析的文本内容
        TokenStream tokenStream = analyzer.tokenStream("", "数据库中存储的数据是高富帅结构化数据，即行数据java，可以用二维表结构来逻辑表达实现的数据。郝小永");
        //3, 设置一个引用，引用可以有多重类型，可以是关键词的引用、偏移量的引用
        CharTermAttribute charTermAttribute=tokenStream.addAttribute(CharTermAttribute.class);
        //偏移量
        OffsetAttribute offsetAttribute=tokenStream.addAttribute(OffsetAttribute.class);
        //4, 调用tokenStream的reset方法
        tokenStream.reset();
        while (tokenStream.incrementToken()){
            System.out.println("start->"+offsetAttribute.startOffset());
            //打印单词
            System.out.println(charTermAttribute);
            System.out.println("end->"+ offsetAttribute.endOffset());
        }
           tokenStream.close();
    }

}
