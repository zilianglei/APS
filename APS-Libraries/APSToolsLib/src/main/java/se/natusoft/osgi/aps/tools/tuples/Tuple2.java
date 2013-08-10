package se.natusoft.osgi.aps.tools.tuples;

/**
 * A tuple with two values.
 */
public class Tuple2<T1, T2> extends Tuple {

    public T2 t2;

    public Tuple2() {}

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }
}
