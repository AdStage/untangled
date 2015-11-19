(ns untangled.test.dom
  (:require [clojure.string :as str]
            [cljs.test :as t :include-macros true]
            [goog.dom :as gd]
            goog.dom.classlist))

(defn- report-addons-dependency-error [e]
  (if (>= (.indexOf (.-message e) "TestUtils") 0)
    (println (str "Error: Untangled DOM tests require React.addons as a dependency in your project."))
    e))

(defn get-attribute
  "
  Get the value of an HTML element's named attribute.

  Parameters:
  `element` A rendered HTML element, as created with `render-as-dom`.
  `attribute` A keyword specifying the attribute you wish to query, i.e. `:value`.

  Returns the attribute value as a string.
  "
  [element attribute]
  (.getAttribute element (name attribute)))


(defn is-rendered-element?
  "Returns a boolean indicating whether the argument is an HTML element that has
  been rendered to the DOM."
  [obj]
  (let [is-dom-element? (gd/isElement obj)
        is-react-element? (js/React.isValidElement obj)
        is-rendered? (.hasOwnProperty obj "getDOMNode")]
    (or is-dom-element? (and is-react-element? is-rendered?))))


(defn render-as-dom
  "Creates a DOM element from a React component."
  [component]
  (js/ReactDOM.findDOMNode (try (js/React.addons.TestUtils.renderIntoDocument component)
                                (catch js/TypeError e (report-addons-dependency-error e)))))


(defn node-contains-text?
  "Returns a boolean indicating whether `dom-node` node (or any of its children) contains `string`."
  [string dom-node]
  (if (or (nil? string) (nil? dom-node))
    false
    (let [regex (js/RegExp. string)
          text (gd/getTextContent dom-node)]
      (.test regex text))))

(defn tag-name [ele] (some-> (.-tagName ele) (str/lower-case)))

(defn find-element
  "
  Finds an HTML element inside a React component or HTML element based on a keyword.

  Parameters:
  *`element`: A rendered HTML element, as created with `render-as-dom`. Note that when an untangled application is
    in test-mode, the rendered DOM is wrapped in a div created in untangled.core. So this function will search your
    entire component, not just its children.
  *`keyword`: defines search type and can be one of:
    *`:key`: the :key hash of the React component, this will look for your `value` as a substring within a data-reactid
             attribute
    *`:{tagname}-text`: look for visible string (as a regex) on a specific tag. E.g. (find-element :button-text \"OK\" dom)
    *`:selector`: any arbitrary CSS selector
    * any attribute name passed as a keyword, i.e. :class will look for your `value` in the class attribute
  *`value`: a string used to find the element based on your :keyword search type

  Returns a rendered HTML element or nil if no match is found.
  "
  [keyword value element]
  (let [keyword-str (name keyword)]
    (cond
      (re-find #"-text$" keyword-str)
      (let [tagname (str/lower-case (re-find #"^\w+" keyword-str))]
        (gd/findNode element (fn [e] (and (node-contains-text? value e) (= tagname (tag-name e))))))
      (= keyword :key) (.querySelector element (str/join ["[data-reactid$='$" value "'"]))
      (= keyword :class) (.querySelector element (str "." value))
      (= keyword :selector) (or (.querySelector element value) nil)
      :else (let [attr (name keyword)
                  selector (str/join ["[" attr "=" value "]"])]
              (or (.querySelector element selector) nil)))))


(defn has-visible-text
  "A test assertion that uses find-element to process search-kind and search-param on the given dom, then
  asserts (cljs.test/is) that the given element has the given text."
  [text-or-regex search-kind search-param dom]
  (if-let [ele (find-element search-kind search-param dom)]
    (if-not (node-contains-text? text-or-regex ele)
      (t/do-report {:type :fail :actual (gd/getTextContent ele) :expected text-or-regex})
      (t/do-report {:type :pass})
      )
    (t/do-report {:type     :error :message (str "Could not find DOM element")
                  :expected (str "DOM element specified by " search-kind " " search-param) :actual "Nil"})
    )
  )

(defn has-element
  "A test assertion that uses find-element to process search-kind and search-param on the given dom, then
  asserts that the returned element is not nil."
  [search-kind search-param dom]
  (if (find-element search-kind search-param dom)
    (t/do-report {:type :pass})
    (t/do-report {:type     :error :message (str "Could not find DOM element")
                  :expected (str "DOM element specified by " search-kind " " search-param) :actual "Nil"})))

(defn has-attribute [attr search-kind search-param dom]
  "A test assertion that uses find-element to process search-kind and search-param on the given dom, then
  asserts that the given element contains the attribute passed in 'attr' as a keyword. This assertion
  DOES NOT test that an attribute has a given value, just that the attribute exists (e.g. 'checked' on an
  input checkbox). To assert that an attribute has a given value, use has-attribute-value."
  (if-let [ele (find-element search-kind search-param dom)]
    (if (get-attribute ele attr)
      (t/do-report {:type :pass})
      (t/do-report {:type   :fail
                    :actual "Attribute not found" :expected (name attr)}))
    (t/do-report {:type     :error :message (str "Could not find DOM element")
                  :expected (str "DOM element specified by " search-kind " " search-param) :actual "Nil"}))
  )

(defn has-attribute-value [attr desired-value search-kind search-param dom]
  "A test assertion that uses find-element to process search-kind and search-param on the given dom, then
  asserts that the given element contains the attribute 'attr=value', where 'attr' is a keyword and
  'value' is a string. DO NOT use this function to test for attributes without an assigned value
  (e.g. 'checked' on an input checkbox), use has-attribute instead."
  (if-let [ele (find-element search-kind search-param dom)]
    (if-let [actual-value (get-attribute ele attr)]
      (if (= actual-value desired-value)
        (t/do-report {:type :pass})
        (t/do-report {:type   :fail
                      :actual (str (name attr) "=" actual-value) :expected (str (name attr) "=" desired-value)}))
      (t/do-report {:type     :error :message (str "Could not find attribute")
                    :expected (str (name attr) "=" (if (nil? desired-value) "nil" desired-value))
                    :actual   (str "Attribute '" (name attr) "' not found")}))
    (t/do-report {:type     :error :message (str "Could not find DOM element")
                  :expected (str "DOM element specified by " search-kind " " search-param) :actual "Nil"})))

(defn has-class [class-name search-kind search-param dom]
  "A test assertion that uses find-element to process search-kind and search-param on the given dom, then
  asserts that the given element contains a class named 'class-name', where 'class-name is a string."
  (if-let [ele (find-element search-kind search-param dom)]
    (if (.contains gd/classlist ele class-name)
      (t/do-report {:type :pass})
      (t/do-report {:type   :fail
                    :actual (str "Class '" class-name "' not found in DOM element specified by " search-kind " " search-param) :expected (str class-name " found in class attribute.")}))
    (t/do-report {:type     :error :message (str "Could not find DOM element")
                  :expected (str "DOM element specified by " search-kind " " search-param) :actual "Nil"})))

(defn has-selected-option [search-type search-value dom-with-select selected-value]
  (if-let [ele (find-element search-type search-value dom-with-select)]
    (if (= "select" (tag-name ele))
      (let [selection (.-value ele)]
        (if (= selection selected-value)
          (t/do-report {:type :pass})
          (t/do-report {:type :fail :actual selection :expected selected-value})
          )
        )
      (t/do-report {:type     :error :message (str "Element at " search-type " " search-value " IS NOT a SELECT")
                    :expected "select" :actual (tag-name ele)
                    })
      )
    (t/do-report {:type     :error :message (str "Could not find a select element at " search-type " " search-value)
                  :expected "DOM element" :actual "Nil"
                  })
    )
  )
