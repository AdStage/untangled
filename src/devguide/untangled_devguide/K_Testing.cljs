(ns untangled-devguide.K-Testing
  (:require-macros [cljs.test :refer [is]]
                   [untangled-devguide.tutmacros :refer [untangled-app]])
  (:require [devcards.core :as dc :include-macros true :refer-macros [defcard defcard-doc dom-node]]
            [cljs.reader :as r]
            [om.next.impl.parser :as p]))

(defcard-doc
  "# Testing

  Untangled includes a library for great BDD. The macros in untangled spec wrap clojure/cljs test, so that you may
  use any of the features of that core library. The specification DSL makes it much easier to read the
  tests, and also includes a number of useful features:

  - Outline rendering
  - Left-to-right assertions
  - More readable output, such as data structure comparisons on failure (with diff notation as well)
  - Real-time refresh of tests on save (client and server)
  - Seeing test results in any number of browsers at once
  - Mocking of normal functions, including native javascript (but as expected: not macros or inline functions)
      - Mocking verifies call sequence and call count
      - Mocks can easily verify arguments received
      - Mocks can simulate timelines for CSP logic

  See Untangled Spec for detailed instructions.
  ")


