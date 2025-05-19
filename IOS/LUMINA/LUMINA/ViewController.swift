//
//  ViewController.swift
//  LUMINA
//
//  Created by 홍석진 on 5/18/25.
//

import UIKit
import WebKit

class ViewController: UIViewController {
    @IBOutlet weak var luminaWebView: WKWebView!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // 로드할 웹 URL 입력
        if let url = URL(string: "https://k12s306.p.ssafy.io/") {
            let request = URLRequest(url: url)
            self.luminaWebView.load(request)
        }
    }
}
