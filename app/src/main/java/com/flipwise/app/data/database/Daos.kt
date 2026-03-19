package com.flipwise.app.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.flipwise.app.data.model.Deck;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class DeckDao_Impl implements DeckDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Deck> __insertionAdapterOfDeck;

  private final EntityDeletionOrUpdateAdapter<Deck> __updateAdapterOfDeck;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDeckById;

  public DeckDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDeck = new EntityInsertionAdapter<Deck>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `decks` (`id`,`name`,`subject`,`color`,`icon`,`createdAt`,`lastStudied`,`cardCount`,`masteredCount`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Deck entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getSubject() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSubject());
        }
        if (entity.getColor() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getColor());
        }
        if (entity.getIcon() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getIcon());
        }
        statement.bindLong(6, entity.getCreatedAt());
        if (entity.getLastStudied() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getLastStudied());
        }
        statement.bindLong(8, entity.getCardCount());
        statement.bindLong(9, entity.getMasteredCount());
      }
    };
    this.__updateAdapterOfDeck = new EntityDeletionOrUpdateAdapter<Deck>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `decks` SET `id` = ?,`name` = ?,`subject` = ?,`color` = ?,`icon` = ?,`createdAt` = ?,`lastStudied` = ?,`cardCount` = ?,`masteredCount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Deck entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getSubject() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSubject());
        }
        if (entity.getColor() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getColor());
        }
        if (entity.getIcon() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getIcon());
        }
        statement.bindLong(6, entity.getCreatedAt());
        if (entity.getLastStudied() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getLastStudied());
        }
        statement.bindLong(8, entity.getCardCount());
        statement.bindLong(9, entity.getMasteredCount());
        if (entity.getId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteDeckById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM decks WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertDeck(final Deck deck, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDeck.insert(deck);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDeck(final Deck deck, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDeck.handle(deck);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDeckById(final String deckId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDeckById.acquire();
        int _argIndex = 1;
        if (deckId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, deckId);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteDeckById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Deck>> getAllDecks() {
    final String _sql = "SELECT * FROM decks ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"decks"}, new Callable<List<Deck>>() {
      @Override
      @NonNull
      public List<Deck> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "icon");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastStudied = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStudied");
          final int _cursorIndexOfCardCount = CursorUtil.getColumnIndexOrThrow(_cursor, "cardCount");
          final int _cursorIndexOfMasteredCount = CursorUtil.getColumnIndexOrThrow(_cursor, "masteredCount");
          final List<Deck> _result = new ArrayList<Deck>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Deck _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            final String _tmpColor;
            if (_cursor.isNull(_cursorIndexOfColor)) {
              _tmpColor = null;
            } else {
              _tmpColor = _cursor.getString(_cursorIndexOfColor);
            }
            final String _tmpIcon;
            if (_cursor.isNull(_cursorIndexOfIcon)) {
              _tmpIcon = null;
            } else {
              _tmpIcon = _cursor.getString(_cursorIndexOfIcon);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpLastStudied;
            if (_cursor.isNull(_cursorIndexOfLastStudied)) {
              _tmpLastStudied = null;
            } else {
              _tmpLastStudied = _cursor.getLong(_cursorIndexOfLastStudied);
            }
            final int _tmpCardCount;
            _tmpCardCount = _cursor.getInt(_cursorIndexOfCardCount);
            final int _tmpMasteredCount;
            _tmpMasteredCount = _cursor.getInt(_cursorIndexOfMasteredCount);
            _item = new Deck(_tmpId,_tmpName,_tmpSubject,_tmpColor,_tmpIcon,_tmpCreatedAt,_tmpLastStudied,_tmpCardCount,_tmpMasteredCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDeckById(final String deckId, final Continuation<? super Deck> $completion) {
    final String _sql = "SELECT * FROM decks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (deckId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, deckId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Deck>() {
      @Override
      @Nullable
      public Deck call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "subject");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "icon");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfLastStudied = CursorUtil.getColumnIndexOrThrow(_cursor, "lastStudied");
          final int _cursorIndexOfCardCount = CursorUtil.getColumnIndexOrThrow(_cursor, "cardCount");
          final int _cursorIndexOfMasteredCount = CursorUtil.getColumnIndexOrThrow(_cursor, "masteredCount");
          final Deck _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpSubject;
            if (_cursor.isNull(_cursorIndexOfSubject)) {
              _tmpSubject = null;
            } else {
              _tmpSubject = _cursor.getString(_cursorIndexOfSubject);
            }
            final String _tmpColor;
            if (_cursor.isNull(_cursorIndexOfColor)) {
              _tmpColor = null;
            } else {
              _tmpColor = _cursor.getString(_cursorIndexOfColor);
            }
            final String _tmpIcon;
            if (_cursor.isNull(_cursorIndexOfIcon)) {
              _tmpIcon = null;
            } else {
              _tmpIcon = _cursor.getString(_cursorIndexOfIcon);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpLastStudied;
            if (_cursor.isNull(_cursorIndexOfLastStudied)) {
              _tmpLastStudied = null;
            } else {
              _tmpLastStudied = _cursor.getLong(_cursorIndexOfLastStudied);
            }
            final int _tmpCardCount;
            _tmpCardCount = _cursor.getInt(_cursorIndexOfCardCount);
            final int _tmpMasteredCount;
            _tmpMasteredCount = _cursor.getInt(_cursorIndexOfMasteredCount);
            _result = new Deck(_tmpId,_tmpName,_tmpSubject,_tmpColor,_tmpIcon,_tmpCreatedAt,_tmpLastStudied,_tmpCardCount,_tmpMasteredCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
