package ru.netology.nework.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.concurrent.AtomicBoolean

class SingleLiveEvent<T> : LiveData<T>() {
    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    public override fun postValue(value: T?) {
        pending.set(true)
        super.postValue(value)
    }
    fun call() {
        value = null
    }
}

