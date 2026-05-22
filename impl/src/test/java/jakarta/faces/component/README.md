# Why these tests live under `jakarta.faces.component`

Most Mojarra tests that exercise Jakarta Faces components live under `org.glassfish.mojarra.component`.
The test classes in this directory are the exceptions: each one needs same-package or protected-member access to a class in the API's `jakarta.faces.component` package,
and would not compile if moved out.

| Test | Reason it cannot move |
| --- | --- |
| `StateHolderSaverTestCase` | Calls the package-private constructor of `jakarta.faces.component.StateHolderSaver`. |
| `UISelectOneTestCase` | Instantiates the package-private `jakarta.faces.component.SelectItemsIterator`. |
| `UIOutputAttachedObjectStateTestCase` | Performs an `instanceof StateHolderSaver` check (also package-private). |
| `UICommandTestCase` | Invokes `UIComponentBase.getFacesListeners(Class)`, which is `protected`. |
| `UIInputTestCase` | Invokes `UIComponentBase.getFacesListeners(Class)`, which is `protected`. |
| `UIViewRootTestCase` | Invokes `UIComponent.addFacesListener(FacesListener)`, which is `protected`. |
| `UIComponentBaseAttachedStateTestCase` | Invokes `UIComponent.addFacesListener(FacesListener)`, which is `protected`. |

Pure API contract tests that don't require this kind of access belong in [`jakartaee/faces`](https://github.com/jakartaee/faces) project.
Tests that exercise Mojarra impl behavior through the API but don't need package-private/protected access belong in `org.glassfish.mojarra.*`.
