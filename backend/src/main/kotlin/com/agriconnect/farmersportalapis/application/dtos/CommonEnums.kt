package com.agriconnect.farmersportalapis.application.dtos

enum class ExportFormat {
    CSV, JSON, XML, PDF, GEOJSON, SHAPEFILE
}

enum class AlertStatisticsGroupBy {
    DAY, WEEK, MONTH, YEAR
}

enum class DocumentStatisticsGroupBy {
    DOCUMENT_TYPE, OWNER_TYPE, UPLOAD_DATE, FILE_TYPE
}