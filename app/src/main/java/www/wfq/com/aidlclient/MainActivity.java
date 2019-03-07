package www.wfq.com.aidlclient;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import www.wfq.com.aidlserver.aidl.Book;
import www.wfq.com.aidlserver.aidl.IBookController;
import www.wfq.com.aidlserver.aidl.IOnNewBookArrivedListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button findBooks, addBook;
    private Boolean isConnected;
    private IBookController controller;
    private List<Book> bookList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findBooks = findViewById(R.id.btn_get);
        addBook = findViewById(R.id.btn_add);
        bindService();
        findBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    bookList = controller.findAllBook();
                    print();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Book book = new Book("客户端新增一本书");
                    controller.addBook(book);
                    print();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void print() {
        for (Book book : bookList) {
            Log.e("WFQ客户端", "value is: " + book);
        }

    }


    // Binder的死亡接收
    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (controller == null)
                return;
            Log.e("WFQ", "binder death");
            // 在这里移除之前绑定的binder，然后重新绑定远程的服务
            controller.asBinder().unlinkToDeath(deathRecipient, 0);
            controller = null;
            bindService();

        }
    };


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            isConnected = true;
            Log.e("WFQ客户端", "连接上服务");
            controller = IBookController.Stub.asInterface(iBinder);
            try {
                controller.registerListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                iBinder.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("WFQ客户端", "断开服务");
            isConnected = false;
        }
    };


    @SuppressLint("HandlerLeak")
    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    Log.e("WFQ", "客户端：收到新增一本新书的通知");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newbook) throws RemoteException {
            mainHandler.obtainMessage(1,newbook).sendToTarget();
        }
    };


    private void bindService() {
        Intent intent = new Intent();
        intent.setPackage("www.wfq.com.aidlserver");
        intent.setAction("www.wfq.com.aidiserver.aidl.BookService");
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private void unBindService() {
        if(controller != null && controller.asBinder().isBinderAlive()){
            try {
                controller.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (isConnected) unbindService(connection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
    }
}