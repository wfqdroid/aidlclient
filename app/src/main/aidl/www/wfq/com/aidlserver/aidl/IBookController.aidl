
// IBookController.aidl
package www.wfq.com.aidlserver.aidl;
import www.wfq.com.aidlserver.aidl.Book;

interface IBookController {


    List<Book> findAllBook();
    //新增一本书 使用定向tag inout
    // AIDL中的定向 tag 表示了在跨进程通信中数据的流向，其中
    // in 表示数据只能由客户端流向服务端，
    // out 表示数据只能由服务端流向客户端，
    // inout 则表示数据可在服务端与客户端之间双向流通
    void addBook(inout Book book);

}
