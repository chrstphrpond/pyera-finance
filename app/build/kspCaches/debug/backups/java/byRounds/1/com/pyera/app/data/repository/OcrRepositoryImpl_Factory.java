package com.pyera.app.data.repository;

import android.content.Context;
import com.pyera.app.domain.ocr.ReceiptParser;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class OcrRepositoryImpl_Factory implements Factory<OcrRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<ReceiptParser> receiptParserProvider;

  public OcrRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<ReceiptParser> receiptParserProvider) {
    this.contextProvider = contextProvider;
    this.receiptParserProvider = receiptParserProvider;
  }

  @Override
  public OcrRepositoryImpl get() {
    return newInstance(contextProvider.get(), receiptParserProvider.get());
  }

  public static OcrRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<ReceiptParser> receiptParserProvider) {
    return new OcrRepositoryImpl_Factory(contextProvider, receiptParserProvider);
  }

  public static OcrRepositoryImpl newInstance(Context context, ReceiptParser receiptParser) {
    return new OcrRepositoryImpl(context, receiptParser);
  }
}
