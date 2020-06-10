SELECT DATE(dateadded) AS "date",
       COUNT(*)
FROM skyscraper.events
GROUP BY "date"
ORDER BY "date" DESC