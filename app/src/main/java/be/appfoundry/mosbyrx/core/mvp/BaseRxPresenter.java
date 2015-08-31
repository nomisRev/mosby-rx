package be.appfoundry.mosbyrx.core.mvp;

import java.util.concurrent.Callable;

import be.appfoundry.mosbyrx.core.rx.RxUtil;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Based on:
 * http://blog.danlew.net/2014/10/08/grokking-rxjava-part-4/
 * https://github.com/sockeqwe/mosby/blob/master/rx/src/main/java/com/hannesdorfmann/mosby/mvp/rx/MvpRxPresenter.java
 *
 * Created by janvancoppenolle on 15/08/15.
 */
public abstract class BaseRxPresenter<V extends BaseMvpView> extends BasePresenter<V> {
    CompositeSubscription subscriptions;

    @Override
    public void attachView(V view) {
        super.attachView(view);
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void detachView(boolean retainInstance) {
        super.detachView(retainInstance);
        if (!retainInstance) {
            subscriptions.unsubscribe();
        }
    }

    protected class RxIOSubscription<T> extends RxSubscription<T> {
       public RxIOSubscription() {
            super(new RxUtil.IOTransformer<T>());
        }
    }

    private class RxSubscription<T> {
        Observable.Transformer<T, T> transformer;

        public RxSubscription(Observable.Transformer<T, T> transformer) {
            this.transformer = transformer;
        }

        public RxSubscription<T> add(Observable<T> observable, Subscriber<T> subscriber) {
            observable = observable.compose(transformer);
            BaseRxPresenter.this.subscriptions.add(observable.subscribe(subscriber));
            return this;
        }

        public RxSubscription<T> add(Callable<T> func, Subscriber<T> subscriber) {
            return add(RxUtil.makeObservable(func), subscriber);
        }
    }
}
