# parc

> `.netrc` support for Clojure

This library provides a parser to convert `.netrc` files into idiomatic Clojure data structures. It also includes utility functions, such as for use with Ring.

## Background

- [What is Ring](https://github.com/ring-clojure/ring#ring)
- [What is `.netrc`](https://everything.curl.dev/usingcurl/netrc)

## The Problem

Many services, like AWS, use custom `credentials` files and formats for storing access details. These credentials often reside in the user's home directory, complicating management, backups, and interoperability.

## The Solution

The `.netrc` file offers a standardized way to describe credentials, addressing these issues with a simple and unified format. Several notable tools, such as `curl`, `GNU Inetutils`, and `heroku-cli`, already support `.netrc`.

Using `.netrc`, you can define credentials for multiple domains, enabling any client to use them regardless of the protocol (HTTP, FTP, etc.). Additionally, `.netrc` files can be encrypted using `gpg` to enhance security (encryption support is planned).

## Usage

Add the library to your `deps.edn`:

Set up default namespaces for examples:

```clojure
(require '[br.dev.zz.parc :as parc]
         '[br.dev.zz.parc.ring :as parc.ring])
```

### Add Credentials to a `ring-request`

By default, `parc.ring/with` searches for credentials in your `~/.netrc` file. If a matching `machine` entry is found, it automatically adds an `Authorization` header to the request.

```clojure
(parc.ring/with {:server-name "example.com"})
=> {:server-name "example.com",
    :headers     {"Authorization" "Basic ZGFuaWVsOnF3ZXJ0eQ=="}}
```

You can also specify a custom `.netrc` file:

```clojure
(parc.ring/with (io/file "custom.netrc") {:server-name "example.com"})
=> {:server-name "example.com",
    :headers     {"Authorization" "Basic ZGFuaWVsOnF3ZXJ0eQ=="}}
```

A custom `.netrc` file might look like this:

```netrc
machine api.example.com
  login my-username
  password my-password
```

### Parsing `.netrc` Files

You can parse a `.netrc` file into a Clojure data structure:

```clojure
(parc/parse (io/file "my-netrc"))
=> [{:machine  "api.example.com"
     :login    "my-username"
     :password "my-password"}]
```

## Usage with Hato

[Hato](https://github.com/gnarroway/hato) is a Clojure HTTP client that supports Ring. You can use `parc.ring/with` to add credentials to a request before passing it to `hato/request`.

```clojure
(-> {:server-name "example.com"
     :scheme      :https
     :server-port 8080}
    parc.ring/with
    hato/request)
```

## References

- [inetutils](https://www.gnu.org/software/inetutils/manual/html_node/The-_002enetrc-file.html)
- [curl](https://everything.curl.dev/usingcurl/netrc)
- [netrc](https://www.labkey.org/Documentation/wiki-page.view?name=netrc)
- [go implementation](https://github.com/heroku/go-netrc)
- [ruby implementation](https://github.com/heroku/netrc)
- [javascript implementation](https://github.com/CamShaft/netrc)
- [node.js implementation](https://github.com/jdxcode/node-netrc-parser)
- [haskell implementation](https://hackage.haskell.org/package/netrc)
- [man page](https://linux.die.net/man/5/netrc)

## Credentials File

> For a lighthearted take on standards, see this [xkcd comic](https://xkcd.com/927/).

- [AWS CLI configuration](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html)
- [GCP authentication](https://cloud.google.com/docs/authentication/getting-started)
- [Azure CLI authentication](https://docs.microsoft.com/en-us/cli/azure/authenticate-azure-cli)

## TODO

- Finalize API with better naming
- Explore integration with `java.net.Authenticator`
- Add `cljc` support
- Support for `clj-http`
- Support for `clj-http.lite`
- Enable parsing of comments
- Add macro support
- Add `gpg` encryption support
