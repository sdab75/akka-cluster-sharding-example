package akka.cluster.sharding.example.app;

import java.io.Serializable;

/**
 * Created by davenkat on 11/8/2015.
 */
public class MyCounter implements Serializable{

    private int count;
    private String msg;

    public MyCounter(){

    }

    public MyCounter(int count, String msg){
        this.count=count;
        this.msg=msg;
    }

    public int getCount() {
        return count;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyCounter myCounter = (MyCounter) o;

        if (count != myCounter.count) return false;
        return !(msg != null ? !msg.equals(myCounter.msg) : myCounter.msg != null);

    }

    @Override
    public int hashCode() {
        int result = count;
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Counter{" +
                "count=" + count +
                ", msg='" + msg + '\'' +
                '}';
    }

    public void setCount(int count) {
        this.count = count;

    }

}
