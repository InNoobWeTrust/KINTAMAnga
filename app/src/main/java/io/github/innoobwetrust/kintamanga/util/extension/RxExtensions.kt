package io.github.innoobwetrust.kintamanga.util.extension

import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.Executors

/**
 * Extension function to quickly achieving parallelization using RxJava
 * variation: buffer on pressure
 * @author InNoobWeTrust @ https://github.com/InNoobWeTrust
 * @see <a href="http://tomstechnicalblog.blogspot.com/2015/11/rxjava-achieving-parallelization.html">RxJava- Achieving Parallelization</a>
 * @see <a href="http://tomstechnicalblog.blogspot.com/2016/02/rxjava-maximizing-parallelization.html">RxJava - Maximizing Parallelization</a>
 */
inline fun <reified T, reified R> Observable<T>.parallelMap(
        crossinline block: (T) -> Observable<R>
): Observable<R> {
    val cores = Runtime.getRuntime().availableProcessors()
    val executor = Executors.newFixedThreadPool(cores)
    return onBackpressureBuffer()
            .observeOn(Schedulers.from(executor))
            .flatMap({ block(it) }, cores)
            .doAfterTerminate { executor.shutdown() }
}