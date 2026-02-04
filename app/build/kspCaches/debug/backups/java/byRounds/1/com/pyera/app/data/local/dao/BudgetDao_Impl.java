package com.pyera.app.data.local.dao;

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
import com.pyera.app.data.local.entity.BudgetEntity;
import com.pyera.app.data.local.entity.BudgetPeriod;
import com.pyera.app.data.local.entity.BudgetSummary;
import com.pyera.app.data.local.entity.BudgetWithSpending;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BudgetDao_Impl implements BudgetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BudgetEntity> __insertionAdapterOfBudgetEntity;

  private final EntityDeletionOrUpdateAdapter<BudgetEntity> __deletionAdapterOfBudgetEntity;

  private final EntityDeletionOrUpdateAdapter<BudgetEntity> __updateAdapterOfBudgetEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBudgetById;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateBudget;

  private final SharedSQLiteStatement __preparedStmtOfActivateBudget;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllBudgetsForUser;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTimestamp;

  public BudgetDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBudgetEntity = new EntityInsertionAdapter<BudgetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `budgets` (`id`,`userId`,`categoryId`,`amount`,`period`,`startDate`,`isActive`,`createdAt`,`updatedAt`,`alertThreshold`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BudgetEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindLong(3, entity.getCategoryId());
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, __BudgetPeriod_enumToString(entity.getPeriod()));
        statement.bindLong(6, entity.getStartDate());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindDouble(10, entity.getAlertThreshold());
      }
    };
    this.__deletionAdapterOfBudgetEntity = new EntityDeletionOrUpdateAdapter<BudgetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `budgets` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BudgetEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBudgetEntity = new EntityDeletionOrUpdateAdapter<BudgetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `budgets` SET `id` = ?,`userId` = ?,`categoryId` = ?,`amount` = ?,`period` = ?,`startDate` = ?,`isActive` = ?,`createdAt` = ?,`updatedAt` = ?,`alertThreshold` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BudgetEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUserId());
        statement.bindLong(3, entity.getCategoryId());
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, __BudgetPeriod_enumToString(entity.getPeriod()));
        statement.bindLong(6, entity.getStartDate());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        statement.bindLong(8, entity.getCreatedAt());
        statement.bindLong(9, entity.getUpdatedAt());
        statement.bindDouble(10, entity.getAlertThreshold());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteBudgetById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM budgets WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeactivateBudget = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE budgets SET isActive = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfActivateBudget = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE budgets SET isActive = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllBudgetsForUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM budgets WHERE userId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTimestamp = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE budgets SET updatedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBudget(final BudgetEntity budget,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfBudgetEntity.insertAndReturnId(budget);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertBudgets(final List<BudgetEntity> budgets,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBudgetEntity.insert(budgets);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBudget(final BudgetEntity budget,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBudgetEntity.handle(budget);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBudget(final BudgetEntity budget,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBudgetEntity.handle(budget);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBudgetById(final int budgetId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBudgetById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, budgetId);
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
          __preparedStmtOfDeleteBudgetById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateBudget(final int budgetId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateBudget.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, budgetId);
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
          __preparedStmtOfDeactivateBudget.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object activateBudget(final int budgetId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfActivateBudget.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, budgetId);
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
          __preparedStmtOfActivateBudget.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllBudgetsForUser(final String userId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllBudgetsForUser.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, userId);
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
          __preparedStmtOfDeleteAllBudgetsForUser.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTimestamp(final int budgetId, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTimestamp.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, budgetId);
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
          __preparedStmtOfUpdateTimestamp.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<BudgetEntity> getBudgetById(final int budgetId) {
    final String _sql = "SELECT * FROM budgets WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, budgetId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets"}, new Callable<BudgetEntity>() {
      @Override
      @Nullable
      public BudgetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfAlertThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "alertThreshold");
          final BudgetEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpCategoryId;
            _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final BudgetPeriod _tmpPeriod;
            _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpAlertThreshold;
            _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
            _result = new BudgetEntity(_tmpId,_tmpUserId,_tmpCategoryId,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpAlertThreshold);
          } else {
            _result = null;
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
  public Object getBudgetByIdSync(final int budgetId,
      final Continuation<? super BudgetEntity> $completion) {
    final String _sql = "SELECT * FROM budgets WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, budgetId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BudgetEntity>() {
      @Override
      @Nullable
      public BudgetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfAlertThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "alertThreshold");
          final BudgetEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpCategoryId;
            _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final BudgetPeriod _tmpPeriod;
            _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpAlertThreshold;
            _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
            _result = new BudgetEntity(_tmpId,_tmpUserId,_tmpCategoryId,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpAlertThreshold);
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

  @Override
  public Flow<BudgetEntity> getActiveBudgetForCategory(final int categoryId) {
    final String _sql = "SELECT * FROM budgets WHERE categoryId = ? AND isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, categoryId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets"}, new Callable<BudgetEntity>() {
      @Override
      @Nullable
      public BudgetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfAlertThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "alertThreshold");
          final BudgetEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpCategoryId;
            _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final BudgetPeriod _tmpPeriod;
            _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpAlertThreshold;
            _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
            _result = new BudgetEntity(_tmpId,_tmpUserId,_tmpCategoryId,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpAlertThreshold);
          } else {
            _result = null;
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
  public Flow<List<BudgetEntity>> getAllBudgetsForUser(final String userId) {
    final String _sql = "SELECT * FROM budgets WHERE userId = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets"}, new Callable<List<BudgetEntity>>() {
      @Override
      @NonNull
      public List<BudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfAlertThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "alertThreshold");
          final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BudgetEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpCategoryId;
            _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final BudgetPeriod _tmpPeriod;
            _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpAlertThreshold;
            _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
            _item = new BudgetEntity(_tmpId,_tmpUserId,_tmpCategoryId,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpAlertThreshold);
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
  public Flow<List<BudgetEntity>> getActiveBudgetsForUser(final String userId) {
    final String _sql = "SELECT * FROM budgets WHERE userId = ? AND isActive = 1 ORDER BY amount DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets"}, new Callable<List<BudgetEntity>>() {
      @Override
      @NonNull
      public List<BudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfAlertThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "alertThreshold");
          final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BudgetEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpCategoryId;
            _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final BudgetPeriod _tmpPeriod;
            _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpAlertThreshold;
            _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
            _item = new BudgetEntity(_tmpId,_tmpUserId,_tmpCategoryId,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpAlertThreshold);
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
  public Flow<List<BudgetEntity>> getBudgetsByPeriod(final BudgetPeriod period,
      final String userId) {
    final String _sql = "SELECT * FROM budgets WHERE period = ? AND userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __BudgetPeriod_enumToString(period));
    _argIndex = 2;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets"}, new Callable<List<BudgetEntity>>() {
      @Override
      @NonNull
      public List<BudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfAlertThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "alertThreshold");
          final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BudgetEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpCategoryId;
            _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final BudgetPeriod _tmpPeriod;
            _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpAlertThreshold;
            _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
            _item = new BudgetEntity(_tmpId,_tmpUserId,_tmpCategoryId,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpAlertThreshold);
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
  public Flow<List<BudgetEntity>> getBudgetsForCategory(final int categoryId) {
    final String _sql = "SELECT * FROM budgets WHERE categoryId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, categoryId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets"}, new Callable<List<BudgetEntity>>() {
      @Override
      @NonNull
      public List<BudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfPeriod = CursorUtil.getColumnIndexOrThrow(_cursor, "period");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfAlertThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "alertThreshold");
          final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BudgetEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final int _tmpCategoryId;
            _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final BudgetPeriod _tmpPeriod;
            _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
            final long _tmpStartDate;
            _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final float _tmpAlertThreshold;
            _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
            _item = new BudgetEntity(_tmpId,_tmpUserId,_tmpCategoryId,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpAlertThreshold);
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
  public Flow<List<BudgetWithSpending>> getBudgetsWithSpending(final String userId,
      final long startDate, final long endDate) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            b.id,\n"
            + "            b.userId,\n"
            + "            b.categoryId,\n"
            + "            c.name as categoryName,\n"
            + "            c.color as categoryColor,\n"
            + "            c.icon as categoryIcon,\n"
            + "            b.amount,\n"
            + "            b.period,\n"
            + "            b.startDate,\n"
            + "            b.isActive,\n"
            + "            b.alertThreshold,\n"
            + "            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as spentAmount,\n"
            + "            b.amount - COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as remainingAmount,\n"
            + "            CASE \n"
            + "                WHEN b.amount > 0 THEN \n"
            + "                    (COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) / b.amount * 100)\n"
            + "                ELSE 0 \n"
            + "            END as progressPercentage,\n"
            + "            CASE \n"
            + "                WHEN COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) > b.amount THEN 1 \n"
            + "                ELSE 0 \n"
            + "            END as isOverBudget,\n"
            + "            0 as daysRemaining\n"
            + "        FROM budgets b\n"
            + "        LEFT JOIN categories c ON b.categoryId = c.id\n"
            + "        LEFT JOIN transactions t ON b.categoryId = t.categoryId \n"
            + "            AND t.date >= ? \n"
            + "            AND t.date <= ?\n"
            + "        WHERE b.userId = ? AND b.isActive = 1\n"
            + "        GROUP BY b.id, b.userId, b.categoryId, c.name, c.color, c.icon, b.amount, b.period, b.startDate, b.isActive, b.alertThreshold\n"
            + "        ORDER BY progressPercentage DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    _argIndex = 3;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"budgets", "categories",
        "transactions"}, new Callable<List<BudgetWithSpending>>() {
      @Override
      @NonNull
      public List<BudgetWithSpending> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
          try {
            final int _cursorIndexOfId = 0;
            final int _cursorIndexOfUserId = 1;
            final int _cursorIndexOfCategoryId = 2;
            final int _cursorIndexOfCategoryName = 3;
            final int _cursorIndexOfCategoryColor = 4;
            final int _cursorIndexOfCategoryIcon = 5;
            final int _cursorIndexOfAmount = 6;
            final int _cursorIndexOfPeriod = 7;
            final int _cursorIndexOfStartDate = 8;
            final int _cursorIndexOfIsActive = 9;
            final int _cursorIndexOfAlertThreshold = 10;
            final int _cursorIndexOfSpentAmount = 11;
            final int _cursorIndexOfRemainingAmount = 12;
            final int _cursorIndexOfProgressPercentage = 13;
            final int _cursorIndexOfIsOverBudget = 14;
            final int _cursorIndexOfDaysRemaining = 15;
            final List<BudgetWithSpending> _result = new ArrayList<BudgetWithSpending>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final BudgetWithSpending _item;
              final int _tmpId;
              _tmpId = _cursor.getInt(_cursorIndexOfId);
              final String _tmpUserId;
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
              final int _tmpCategoryId;
              _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
              final String _tmpCategoryName;
              _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
              final int _tmpCategoryColor;
              _tmpCategoryColor = _cursor.getInt(_cursorIndexOfCategoryColor);
              final String _tmpCategoryIcon;
              if (_cursor.isNull(_cursorIndexOfCategoryIcon)) {
                _tmpCategoryIcon = null;
              } else {
                _tmpCategoryIcon = _cursor.getString(_cursorIndexOfCategoryIcon);
              }
              final double _tmpAmount;
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
              final BudgetPeriod _tmpPeriod;
              _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
              final long _tmpStartDate;
              _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
              final boolean _tmpIsActive;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsActive);
              _tmpIsActive = _tmp != 0;
              final float _tmpAlertThreshold;
              _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
              final double _tmpSpentAmount;
              _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
              final double _tmpRemainingAmount;
              _tmpRemainingAmount = _cursor.getDouble(_cursorIndexOfRemainingAmount);
              final float _tmpProgressPercentage;
              _tmpProgressPercentage = _cursor.getFloat(_cursorIndexOfProgressPercentage);
              final boolean _tmpIsOverBudget;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsOverBudget);
              _tmpIsOverBudget = _tmp_1 != 0;
              final int _tmpDaysRemaining;
              _tmpDaysRemaining = _cursor.getInt(_cursorIndexOfDaysRemaining);
              _item = new BudgetWithSpending(_tmpId,_tmpUserId,_tmpCategoryId,_tmpCategoryName,_tmpCategoryColor,_tmpCategoryIcon,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpAlertThreshold,_tmpSpentAmount,_tmpRemainingAmount,_tmpProgressPercentage,_tmpIsOverBudget,_tmpDaysRemaining);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<BudgetWithSpending> getBudgetWithSpendingById(final int budgetId,
      final long startDate, final long endDate) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            b.id,\n"
            + "            b.userId,\n"
            + "            b.categoryId,\n"
            + "            c.name as categoryName,\n"
            + "            c.color as categoryColor,\n"
            + "            c.icon as categoryIcon,\n"
            + "            b.amount,\n"
            + "            b.period,\n"
            + "            b.startDate,\n"
            + "            b.isActive,\n"
            + "            b.alertThreshold,\n"
            + "            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as spentAmount,\n"
            + "            b.amount - COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as remainingAmount,\n"
            + "            CASE \n"
            + "                WHEN b.amount > 0 THEN \n"
            + "                    (COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) / b.amount * 100)\n"
            + "                ELSE 0 \n"
            + "            END as progressPercentage,\n"
            + "            CASE \n"
            + "                WHEN COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) > b.amount THEN 1 \n"
            + "                ELSE 0 \n"
            + "            END as isOverBudget,\n"
            + "            0 as daysRemaining\n"
            + "        FROM budgets b\n"
            + "        LEFT JOIN categories c ON b.categoryId = c.id\n"
            + "        LEFT JOIN transactions t ON b.categoryId = t.categoryId \n"
            + "            AND t.date >= ? \n"
            + "            AND t.date <= ?\n"
            + "        WHERE b.id = ?\n"
            + "        GROUP BY b.id, b.userId, b.categoryId, c.name, c.color, c.icon, b.amount, b.period, b.startDate, b.isActive, b.alertThreshold\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    _argIndex = 3;
    _statement.bindLong(_argIndex, budgetId);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"budgets", "categories",
        "transactions"}, new Callable<BudgetWithSpending>() {
      @Override
      @Nullable
      public BudgetWithSpending call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
          try {
            final int _cursorIndexOfId = 0;
            final int _cursorIndexOfUserId = 1;
            final int _cursorIndexOfCategoryId = 2;
            final int _cursorIndexOfCategoryName = 3;
            final int _cursorIndexOfCategoryColor = 4;
            final int _cursorIndexOfCategoryIcon = 5;
            final int _cursorIndexOfAmount = 6;
            final int _cursorIndexOfPeriod = 7;
            final int _cursorIndexOfStartDate = 8;
            final int _cursorIndexOfIsActive = 9;
            final int _cursorIndexOfAlertThreshold = 10;
            final int _cursorIndexOfSpentAmount = 11;
            final int _cursorIndexOfRemainingAmount = 12;
            final int _cursorIndexOfProgressPercentage = 13;
            final int _cursorIndexOfIsOverBudget = 14;
            final int _cursorIndexOfDaysRemaining = 15;
            final BudgetWithSpending _result;
            if (_cursor.moveToFirst()) {
              final int _tmpId;
              _tmpId = _cursor.getInt(_cursorIndexOfId);
              final String _tmpUserId;
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
              final int _tmpCategoryId;
              _tmpCategoryId = _cursor.getInt(_cursorIndexOfCategoryId);
              final String _tmpCategoryName;
              _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
              final int _tmpCategoryColor;
              _tmpCategoryColor = _cursor.getInt(_cursorIndexOfCategoryColor);
              final String _tmpCategoryIcon;
              if (_cursor.isNull(_cursorIndexOfCategoryIcon)) {
                _tmpCategoryIcon = null;
              } else {
                _tmpCategoryIcon = _cursor.getString(_cursorIndexOfCategoryIcon);
              }
              final double _tmpAmount;
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
              final BudgetPeriod _tmpPeriod;
              _tmpPeriod = __BudgetPeriod_stringToEnum(_cursor.getString(_cursorIndexOfPeriod));
              final long _tmpStartDate;
              _tmpStartDate = _cursor.getLong(_cursorIndexOfStartDate);
              final boolean _tmpIsActive;
              final int _tmp;
              _tmp = _cursor.getInt(_cursorIndexOfIsActive);
              _tmpIsActive = _tmp != 0;
              final float _tmpAlertThreshold;
              _tmpAlertThreshold = _cursor.getFloat(_cursorIndexOfAlertThreshold);
              final double _tmpSpentAmount;
              _tmpSpentAmount = _cursor.getDouble(_cursorIndexOfSpentAmount);
              final double _tmpRemainingAmount;
              _tmpRemainingAmount = _cursor.getDouble(_cursorIndexOfRemainingAmount);
              final float _tmpProgressPercentage;
              _tmpProgressPercentage = _cursor.getFloat(_cursorIndexOfProgressPercentage);
              final boolean _tmpIsOverBudget;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsOverBudget);
              _tmpIsOverBudget = _tmp_1 != 0;
              final int _tmpDaysRemaining;
              _tmpDaysRemaining = _cursor.getInt(_cursorIndexOfDaysRemaining);
              _result = new BudgetWithSpending(_tmpId,_tmpUserId,_tmpCategoryId,_tmpCategoryName,_tmpCategoryColor,_tmpCategoryIcon,_tmpAmount,_tmpPeriod,_tmpStartDate,_tmpIsActive,_tmpAlertThreshold,_tmpSpentAmount,_tmpRemainingAmount,_tmpProgressPercentage,_tmpIsOverBudget,_tmpDaysRemaining);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<BudgetSummary> getBudgetSummary(final String userId, final long startDate,
      final long endDate) {
    final String _sql = "\n"
            + "        SELECT \n"
            + "            COUNT(*) as totalBudgets,\n"
            + "            COALESCE(SUM(b.amount), 0) as totalBudgetAmount,\n"
            + "            COALESCE(SUM(spent.spentAmount), 0) as totalSpent,\n"
            + "            COALESCE(SUM(b.amount), 0) - COALESCE(SUM(spent.spentAmount), 0) as totalRemaining,\n"
            + "            CASE \n"
            + "                WHEN COALESCE(SUM(b.amount), 0) > 0 THEN \n"
            + "                    (COALESCE(SUM(spent.spentAmount), 0) / COALESCE(SUM(b.amount), 0) * 100)\n"
            + "                ELSE 0 \n"
            + "            END as overallProgress,\n"
            + "            SUM(CASE WHEN spent.spentAmount > b.amount THEN 1 ELSE 0 END) as overBudgetCount,\n"
            + "            SUM(CASE WHEN spent.spentAmount >= b.amount * b.alertThreshold AND spent.spentAmount <= b.amount THEN 1 ELSE 0 END) as warningCount,\n"
            + "            SUM(CASE WHEN spent.spentAmount < b.amount * b.alertThreshold THEN 1 ELSE 0 END) as healthyCount\n"
            + "        FROM budgets b\n"
            + "        LEFT JOIN (\n"
            + "            SELECT \n"
            + "                categoryId,\n"
            + "                SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as spentAmount\n"
            + "            FROM transactions\n"
            + "            WHERE date >= ? AND date <= ?\n"
            + "            GROUP BY categoryId\n"
            + "        ) spent ON b.categoryId = spent.categoryId\n"
            + "        WHERE b.userId = ? AND b.isActive = 1\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    _argIndex = 3;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets",
        "transactions"}, new Callable<BudgetSummary>() {
      @Override
      @NonNull
      public BudgetSummary call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTotalBudgets = 0;
          final int _cursorIndexOfTotalBudgetAmount = 1;
          final int _cursorIndexOfTotalSpent = 2;
          final int _cursorIndexOfTotalRemaining = 3;
          final int _cursorIndexOfOverallProgress = 4;
          final int _cursorIndexOfOverBudgetCount = 5;
          final int _cursorIndexOfWarningCount = 6;
          final int _cursorIndexOfHealthyCount = 7;
          final BudgetSummary _result;
          if (_cursor.moveToFirst()) {
            final int _tmpTotalBudgets;
            _tmpTotalBudgets = _cursor.getInt(_cursorIndexOfTotalBudgets);
            final double _tmpTotalBudgetAmount;
            _tmpTotalBudgetAmount = _cursor.getDouble(_cursorIndexOfTotalBudgetAmount);
            final double _tmpTotalSpent;
            _tmpTotalSpent = _cursor.getDouble(_cursorIndexOfTotalSpent);
            final double _tmpTotalRemaining;
            _tmpTotalRemaining = _cursor.getDouble(_cursorIndexOfTotalRemaining);
            final float _tmpOverallProgress;
            _tmpOverallProgress = _cursor.getFloat(_cursorIndexOfOverallProgress);
            final int _tmpOverBudgetCount;
            _tmpOverBudgetCount = _cursor.getInt(_cursorIndexOfOverBudgetCount);
            final int _tmpWarningCount;
            _tmpWarningCount = _cursor.getInt(_cursorIndexOfWarningCount);
            final int _tmpHealthyCount;
            _tmpHealthyCount = _cursor.getInt(_cursorIndexOfHealthyCount);
            _result = new BudgetSummary(_tmpTotalBudgets,_tmpTotalBudgetAmount,_tmpTotalSpent,_tmpTotalRemaining,_tmpOverallProgress,_tmpOverBudgetCount,_tmpWarningCount,_tmpHealthyCount);
          } else {
            _result = null;
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
  public Flow<Integer> getActiveBudgetCount(final String userId) {
    final String _sql = "SELECT COUNT(*) FROM budgets WHERE userId = ? AND isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Flow<Integer> getOverBudgetCount(final String userId, final long startDate,
      final long endDate) {
    final String _sql = "\n"
            + "        SELECT COUNT(*) FROM budgets WHERE userId = ? AND isActive = 1 AND id IN (\n"
            + "            SELECT b.id FROM budgets b\n"
            + "            LEFT JOIN transactions t ON b.categoryId = t.categoryId \n"
            + "                AND t.date >= ? AND t.date <= ? AND t.type = 'EXPENSE'\n"
            + "            GROUP BY b.id\n"
            + "            HAVING COALESCE(SUM(t.amount), 0) > b.amount\n"
            + "        )\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 3;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"budgets",
        "transactions"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __BudgetPeriod_enumToString(@NonNull final BudgetPeriod _value) {
    switch (_value) {
      case DAILY: return "DAILY";
      case WEEKLY: return "WEEKLY";
      case MONTHLY: return "MONTHLY";
      case YEARLY: return "YEARLY";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private BudgetPeriod __BudgetPeriod_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "DAILY": return BudgetPeriod.DAILY;
      case "WEEKLY": return BudgetPeriod.WEEKLY;
      case "MONTHLY": return BudgetPeriod.MONTHLY;
      case "YEARLY": return BudgetPeriod.YEARLY;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
