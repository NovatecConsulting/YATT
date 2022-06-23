import shared
import SwiftUI

@main
struct YattApp: App {

    init() {
        KoinIosKt.doInitKoinIos()
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
