(ns cards.websocket-cards
  (:require
    [devcards.core :as dc :include-macros true]
    [recipes.websockets-client :as client]
    [untangled.client.cards :refer [untangled-app]]
    [om.dom :as dom]
    [untangled.client.data-fetch :as df]
    [untangled.client.logging :as log]
    [untangled.client.core :as uc]
    [om.next :as om]
    [untangled.websockets.networking :as wn]))

(dc/defcard websocket-card
  "
  # Websockets

  Note: This is a full-stack example. Make sure you're running the server and are serving this page from it. The server
  had additional custom augments for this demo, as explained later in this page.

  This is a demo chat-style app. Open this page in more than one tab/browser to see the result.

  Websockets act as an alternate networking mechanism for Untangled apps. As such, they change nothing about how
  you write the majority of your application. Everything is still done with the same UI and mutation logic you've
  already been doing.

  Websockets ensure that each client has a persistent TCP connection to the server, and thus allow the server to push
  updates to the client. The client handles these via a predefined multimethod as described below.
  "
  (untangled-app client/Root
    :networking (wn/make-channel-client "/chsk" :global-error-callback (constantly nil))
    :started-callback (fn [{:keys [reconciler]}]
                        (df/load reconciler :app/channels client/Channel {:refresh [:app/channels]})
                        (df/load reconciler :app/users client/User {:refresh [:app/users]})))
  {}
  {:inspect-data false})

(dc/defcard-doc
  "# Websockets Setup

  There are several steps for setting up Untangled to use websockets as the primary mechanism for app communication:

  1. Set up the client to use websocket networking.
  2. Add websockets support to your server
  3. Add handlers to the client for receiving server push messages

  ## Setting up the client

  This is probably the simplest step. Just add the `:networking` option when creating the client:

  ```
  (uc/new-untangled-client
    :networking (wn/make-channel-client \"/chsk\" :global-error-callback (constantly nil))
    ...)
  ```

  The default route for establishing websockets is `/chsk`. The internals use Sente to provide the websockets.

  ## Adding Server Support

  The server needs a couple of components and a hook for an extra route:

  ```
  (core/make-untangled-server
    ; provides the URI on which you've configured the client to connect:
    :extra-routes {:routes   [" " {[\"/chsk\"] :web-socket}]
                   :handlers {:web-socket cs/route-handlers}}
    ; Adds the components needed in order to establish and work with clients on persistent sockets
    :components {:channel-server   (cs/make-channel-server)
                 :channel-listener (wsdemo/make-channel-listener)}))
  ```

  The channel listner is something you create: a component that is told when a client connects or drops. This will allow
  you to manage your own internal data structures that track your active users.

  Typically this will be a component that injects the channel server. This demo defines it as follows:

  ```
  (defrecord ChannelListener [channel-server subscriptions]
    WSListener
    (client-dropped [this ws-net cid] ...)
    (client-added [this ws-net cid] ...)

    component/Lifecycle
    (start [component]
      (let [component (assoc component
                        :subscriptions (atom {}))]
        (add-listener channel-server component)
        component))
    (stop [component]
      (remove-listener channel-server component)
      (dissoc component :subscriptions :kill-chan)))

  (defn make-channel-listener []
    (component/using
      (map->ChannelListener {})
      [:channel-server]))
  ```

  Note that the channel server is injected into the component and the `start`/`stop` methods use it to add/remove
  the component as a listener of connect/drop events. The `ws-net` parameter is the channel server which
  implements the WSNet protocol:

  ```
  (defprotocol WSNet
    (add-listener [this ^WSListener listener] \"Add a `WSListener` listener\")
    (remove-listener [this ^WSListener listener] \"Remove a `WSListener` listener\")
    (push [this cid verb edn] \"Push from server\"))
  ```

  and it is this push method that is most interesting to us. It allows the server to push messages to a specific user
  by client id (cid). The `verb` and `edn` parameters are what will arrive on the client.

  ## Handling Push Messages

  Untangled treats incoming push messages much like mutations, though on a different multimethod
  `untangled.websockets.networking/push-received`. The parameters to this method are the untangled `app` and the
  `message` (which contains the keywords `:topic` with the verb from the server and `:msg` with the EDN. The `:topic`
  is used as the dispatch key for the multimethod).

  So, a call on the server to `(push ws-net client-id :hello {:name \"Jo\"})` will result in a call on the client
  of `(push-received untangled-app {:topic :hello :msg {:name \"Jo\"}})`.
  "
  (dc/mkdn-pprint-source client/Root)
  )

