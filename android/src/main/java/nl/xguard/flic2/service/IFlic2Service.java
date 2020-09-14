package nl.xguard.flic2.service;


import io.reactivex.Observable;

public interface IFlic2Service {

    Observable<Boolean> flic2IsInitialized();
}
