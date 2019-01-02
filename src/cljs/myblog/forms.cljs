(ns myblog.forms
  (:require [clojure.string :refer [blank?]]))


(defn date? [date-str]
  (not (js/isNaN (js/Date. date-str))))


(defn add-errors [db error-map]
  (reduce-kv
    #(assoc-in %1 [:metadata-form %2 :error] %3)
    db
    error-map))


(defn clear-form-errors [db]
  (let [fields [:title-image :title-image-srcset :slug :publish-override]]
    (reduce
      #(assoc-in %1 [:metadata-form %2 :error] "")
      db
      fields)))


(defn copy-latest-values
  "Copy the current metadata from the article (if present)
  into the metadata form."
  [db]
  (let [article (:article db)
        fields [:title-image :slug :publish-override :title-image-srcset]]
    (reduce
      #(assoc-in %1 [:metadata-form %2 :value] (%2 article))
      db
      fields)))


(defn canonicalize-date
  "Coerce a date string into into a proper iso string if present"
  [date-str]
  (if (blank? date-str)
    nil
    (-> date-str js/Date. (.toISOString) (.slice 0 -1))))


(defn maybe-canonicalize-date [{:keys [publish-override] :as metadata}]
  (if publish-override
    (assoc metadata :publish-override (canonicalize-date publish-override))
    metadata))


(defn merge-metadata
  "Merge the form metadata into the article object in
  preparation for saving."
  [db]
  (let [article (:article db)
        {:keys [slug publish-override title-image title-image-srcset]} (:metadata-form db)]
    (merge article {:slug (:value slug)
                    :publish-override (-> publish-override :value canonicalize-date)
                    :title-image (:value title-image)
                    :title-image-srcset (:value title-image-srcset)})))



(defn valid-metadata?
  "Sanity check that title images and slugs are not blank
  and that the override, if present, is a parsable date."
  [form-data]
  (let [slug (get-in form-data [:slug :value])
        title-image (get-in form-data [:title-image :value])
        publish-override (get-in form-data [:publish-override :value])

        possible-errors {:slug             (when (blank? slug) "cannot be blank")
                         :title-image      (when (blank? title-image) "cannot be blank")
                         :publish-override (when (not (blank? publish-override))
                                             (when (not (date? publish-override))
                                               "must be a valid ISO Date"))}
        actual-errors (->> possible-errors
                           (filter (fn [[k v]] (not (nil? v))))
                           (into {}))]
    (if (empty? actual-errors)
      nil
      actual-errors)))
