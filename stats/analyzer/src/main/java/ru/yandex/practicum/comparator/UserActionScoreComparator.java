package ru.yandex.practicum.comparator;

import ru.yandex.practicum.model.UserAction;

import java.util.Comparator;

public class UserActionScoreComparator implements Comparator<UserAction> {

    @Override
    public int compare(UserAction o1, UserAction o2) {
        return Double.compare(o1.getWeight(), o2.getWeight());
    }
}
