// IOnNewBookArrivedListener.aidl
package www.wfq.com.aidlserver.aidl;
import www.wfq.com.aidlserver.aidl.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newbook);
}
