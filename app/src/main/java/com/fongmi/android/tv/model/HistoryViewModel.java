package com.fongmi.android.tv.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.db.AppDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class HistoryViewModel extends ViewModel {

    private final MutableLiveData<List<History>> histories;
    private final MutableLiveData<Boolean> hasChanges;

    public HistoryViewModel() {
        histories = new MutableLiveData<>();
        hasChanges = new MutableLiveData<>(false);
    }

    public LiveData<List<History>> getHistories() {
        return histories;
    }

    public LiveData<Boolean> getHasChanges() {
        return hasChanges;
    }

    public void loadHistories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<History> items = History.get();
            histories.postValue(items);
        });
    }

    public void deleteHistory(History item) {
        Executors.newSingleThreadExecutor().execute(() -> {
            item.delete();
            loadHistories();
            hasChanges.postValue(true);
        });
    }

    public void clearHistories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.get().getHistoryDao().delete();
            loadHistories();
            hasChanges.postValue(true);
        });
    }
}
