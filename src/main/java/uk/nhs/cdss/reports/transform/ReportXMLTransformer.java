package uk.nhs.cdss.reports.transform;

public interface ReportXMLTransformer<T> {

  String transform(T input) throws TransformationException;
}
