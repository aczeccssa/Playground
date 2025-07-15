//  RepresentableHypermarket.swift
//
//  Created by Lester E on 2023/6/18.
//

import Foundation

#if os(macOS)
import SystemConfiguration
#else
import FoundationNetworking
#endif

// Get user documents dir
func getDocumentsDirectory() -> URL {
    let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
    return paths[0]
}

// Read file content from specific path
func readSpecificRestoreFile(fileName: String) throws -> String {
    let currentDirectoryURL = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
    let fileURL = currentDirectoryURL.appendingPathComponent(fileName)

    do {
        return try String(contentsOf: fileURL, encoding: .utf8)
    } catch {
        throw NSError(domain: "IOError", code: 2, userInfo: [NSLocalizedDescriptionKey: "Failed to read file: \(fileURL.path), Error: \(error.localizedDescription)"])
    }
}

// Restore data to documents dir
func restoreBackupAliceData(restoreFileName: String, targetFileName: String) -> Bool {
    let fileManager = FileManager.default
    let documentsURL = getDocumentsDirectory()
    let targetFileURL = documentsURL.appendingPathComponent(targetFileName)
    do {
        // Delete if exisit
        if fileManager.fileExists(atPath: targetFileURL.path) {
            try fileManager.removeItem(at: targetFileURL)
        }

        // Read current dir content
        let content = try readSpecificRestoreFile(fileName: restoreFileName)
        let fileContent = content.data(using: .utf8)!

        // Write to document dir
        try fileContent.write(to: targetFileURL, options: .atomic)
        print("Restore successfully: \(targetFileURL)")
        return true
    } catch {
        print("Restore failed: \(error.localizedDescription)")
        return false
    }
}

public struct savedAliceData: Codable {
    public let standardAccountArr: [standardAccount]
    public let merchantAccountArr: [merchantAccount]
    public let productArr: [ProductItem]
    public let otherArr: [otherForm]

    let updateDate: Date

    public init(standardAccountArr: [standardAccount], merchantAccountArr: [merchantAccount], productArr: [ProductItem], otherArr: [otherForm]) {
        self.standardAccountArr = standardAccountArr
        self.merchantAccountArr = merchantAccountArr
        self.productArr = productArr
        self.otherArr = otherArr

        self.updateDate = .now
    }

    var updateDateStr: String {
        "\(self.updateDate)"
    }
}

//readline's fitness function

public struct inputFuncses {
    public static func inputDouble(_ showTitle: String? = nil) -> Double {
        if let title = showTitle {
            print(title)
        }
        var isOver = false
        var result: Double = 0
        repeat {
            guard let str = readLine() else {
                print("You must enter something")
                continue
            }
            if let pander = Double(str) {
                result = pander
                isOver = true
                return result
            } else {
                print("You must make sure the content is a number")
                continue
            }
        } while !isOver
    }

    public static func inputBasicType(_ showTitle: String? = nil, _ showTypes: Bool = false) -> BasicProductType {
        if let title = showTitle {
            print(title)
        }
        if showTypes {
            print(BasicProductType.getProductTypesList().description)
        }

        var isOver = false
        var result: BasicProductType = BasicProductType.Bags
        repeat {
            guard let str = readLine() else {
                print("You must enter something")
                continue
            }
            for staticType in BasicProductType.allCases {
                if staticType.rawValue.lowercased() == str.lowercased() {
                    result = staticType
                    isOver = true
                    return result
                } else {
                    continue
                }
            }
            print("You must enter a static type")
        } while !isOver
    }

    public static func inputString(_ showTitle: String? = nil, endl: String? = nil) -> String {
        if let title = showTitle {
            print(title)
        }
        var isOver = false
        var result: String = ""
        repeat {
            if let str = readLine() {
                isOver = true
                result = str

                if let ending = endl {
                    print(ending)
                }
                return result
            } else {
                print("You must enter something")
                continue
            }
        } while !isOver
    }
}

// Foundations

public func isInternetAvailable() -> Bool {
    if let url = URL(string: "https://www.apple.com") {
        var request = URLRequest(url: url)
        request.timeoutInterval = 5.0
        request.httpMethod = "HEAD"
        let semaphore = DispatchSemaphore(value: 0)
        var isConnected = false

        URLSession.shared.dataTask(with: request) { (data, response, error) in
            if let _ = response as? HTTPURLResponse, error == nil {
                isConnected = true
            }
            semaphore.signal()
        }
        .resume()

        let _ = semaphore.wait(timeout: .now() + 5.0)
        return isConnected
    }
    return false
}

public func randomString(length: Int? = nil, allowDigits: Bool = true) -> String {
    let letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    let allowedChars = allowDigits ? "\(letters)0123456789" : letters
    let length = max(1, length ?? Int.random(in: 1...255))
    var randomString = ""
    for _ in 0..<length {
        let randomIndex = Int.random(in: 0..<allowedChars.count)
        let randomChar = allowedChars[allowedChars.index(allowedChars.startIndex, offsetBy: randomIndex)]
        randomString.append(randomChar)
    }
    return randomString
}

public func saveOtherManagerToFile(_ savedAliceData: savedAliceData) {
    do {
        let jsonEncoder = JSONEncoder()

        // Encode SavedAliceData Object to JSON data.
        guard let jsonData = try? jsonEncoder.encode(savedAliceData) else {
            throw NSError(domain: "EncodingError", code: 1, userInfo: [NSLocalizedDescriptionKey: "Failed to encode SavedAliceData as JSON."])
        }

        // Get system `Documents` dir.
        let fileManager = FileManager.default
        guard let documentsDirectory = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else {
            throw NSError(domain: "FileManagerError", code: 2, userInfo: [NSLocalizedDescriptionKey: "Failed to get documents directory."])
        }

        // Build file path
        let fileName = ".repSupermarketSavedAliceData.json"
        let filePath = documentsDirectory.appendingPathComponent(fileName)

        // Write JSON data to file.
        try jsonData.write(to: filePath, options: .atomic)
        print("Successfully saved savedAliceData to file \(filePath.absoluteString).")
    } catch let error {
        fatalError("Failed to write json data: \(error)")
    }
}

public func loadJsonDataFromURL(url: String? = nil) -> savedAliceData? {
    var data: Data?

    if let urlString = url {
        // Load remote JSON data
        if let url = URL(string: urlString) {
            do {
                data = try Data(contentsOf: url)
                print("Loaded remote JSON data.")
            } catch {
                print("Error reading remote JSON file: \(error.localizedDescription)")
            }
        } else {
            print("Invalid URL")
        }
    } else {
        // Load local JSON data
        if let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first {
            let fileURL = documentsDirectory.appendingPathComponent(".repSupermarketSavedAliceData.json")

            do {
                data = try Data(contentsOf: fileURL)
                print("Loaded local JSON data from file.")
            } catch {
                print("Error decoding local JSON file: \(error.localizedDescription)")
            }
        } else {
            print("Error: Unable to access documents directory.")
        }
    }

    // Check if loaded data is empty
    if let jsonData = data, !jsonData.isEmpty {
        let decoder = JSONDecoder()
        do {
            let aliceData = try decoder.decode(savedAliceData.self, from: jsonData)
            print("Loaded JSON data.")
            return aliceData
        } catch {
            print("Error decoding JSON data: \(error.localizedDescription)")
        }
    } else {
        print("Error: Empty JSON data.")
    }

    return nil
}

protocol BasicProduct {
    var ProductName: String { get }
    var discount: Double { get }
    var ProductType: BasicProductType { get }
    var ProductDescription: String { get }
}


//Other Manager

public class otherForm: Identifiable, Codable, Comparable, Equatable, CustomStringConvertible {
    private let PManager: ProductManager
    private let MAManager: MerchantAccountManager
    private let SAManager: StandardAccountManager
    public let id: UUID
    private let _creationDate: Date
    private let _otherProduct: UUID
    private let _ProductMerchant: UUID
    private let _OtherCreator: UUID
    private var _statment: OtherState {
        didSet {
            if _statment == .Done {
                self._finishDate = .now
            }
        }
    }
    private var _finishDate: Date?

    init(product: UUID, merchant: UUID, creator: UUID) {
        //stand
        self.PManager = ProductManager()
        self.MAManager = MerchantAccountManager()
        self.SAManager = StandardAccountManager()
        //source
        self.id = UUID()
        self._creationDate = .now
        self._otherProduct = product
        self._ProductMerchant = merchant
        self._OtherCreator = creator
        self._statment = .Progress
        self._finishDate = nil
    }

    var creationDate: Date {
        self._creationDate
    }

    var Statment: OtherState {
        self._statment
    }

    var OtherProduct: String {
        if let result = PManager.aliveProducts.filter({ $0.getProductId() == self._otherProduct }).first {
            return result.getProductName()
        }
        return ""
    }

    var ProductMerchant: String {
        if let result = MAManager.analysisMerchantID(accountId: self._ProductMerchant) {
            return result.getAccountName()
        }
        return ""
    }

    var OtherCreator: String {
        if let result = SAManager.analysisStandardID(accountId: self._OtherCreator) {
            return result.getAccountName()
        }
        return ""
    }

    public var description: String {
        "Product: \(self.OtherProduct); Merchant: \(self.ProductMerchant); Creator: \(self.OtherCreator)"
    }

    public static func == (lhs: otherForm, rhs: otherForm) -> Bool {
        lhs.id == rhs.id
    }

    public static func < (lhs: otherForm, rhs: otherForm) -> Bool {
        lhs._creationDate < rhs.creationDate
    }

    func getOtherId() -> UUID {
        self.id
    }

    func updateOtherState(state: String) -> OtherState? {
        for staticState in OtherState.allCases {
            if staticState.rawValue.lowercased() == state.lowercased() {
                self._statment = staticState
                return staticState
            }
        }
        return nil
    }

}

public class OtherManager: Codable {
    private let PManager: ProductManager
    private let SAManager: StandardAccountManager
    private let MAManager: MerchantAccountManager
    //Notavaliable to create all managerment in all manger
    private static var _otherList: [otherForm] = []
    private static var _completedOrder: [otherForm] {
        Self._otherList.filter({ $0.Statment.rawValue != "Done" || $0.Statment.rawValue != "Canceled" })
    }
    private static var _inompleteOrder: [otherForm] {
        Self._otherList.filter({ $0.Statment.rawValue == "Done" || $0.Statment.rawValue == "Canceled" })
    }

    public init() {
        self.PManager = ProductManager()
        self.SAManager = StandardAccountManager()
        self.MAManager = MerchantAccountManager()
    }

    var CompletedOrder: [otherForm] {
        Self._completedOrder
    }
    var InompleteOrder: [otherForm] {
        Self._inompleteOrder
    }

    func newOtherEvent(product: UUID, merchant: UUID, creator: UUID) -> UUID {
        let newOther = otherForm(product: product, merchant: merchant, creator: creator)
        Self._otherList.append(newOther)
        return newOther.getOtherId()
    }

    func analysisOtherID(OtherId: UUID) -> otherForm? {
        guard let index = Self._otherList.firstIndex(where: { $0.getOtherId() == OtherId }) else {
            return nil
        }
        let otherPointer = withUnsafeMutablePointer(to: &Self._otherList[index], { $0 })
        return otherPointer.pointee
    }

    func canceledOther(otherId: UUID) -> OtherState? {
        if let other = Self._otherList.filter({ $0.getOtherId() == otherId }).first {
            return other.updateOtherState(state: "canceled")
        }
        return nil
    }

    public func getOtherDetail(otherId: UUID) -> String? {
        guard let other = self.analysisOtherID(OtherId: otherId) else {
            return nil
        }
        return other.description
    }

    public static func backupAlice() -> [otherForm] {
        Self._otherList
    }

    public static func reloadStaticAlice(aliceData: [otherForm]) {
        if aliceData.isEmpty {
            print("Not avaliable to load a empty data")
        } else {
            print("Other: Success to load \(aliceData.count) data")
            Self._otherList = aliceData
        }
    }
}

public class ProductItemData: Identifiable, BasicProduct, Codable {
    public let id: UUID
    private var _aliveSatment: Bool
    private var _ProductName: String {
        willSet {
            self.updateBackup()
        }
        didSet {
            self._ReleaseUpdateDate = Date.now
        }
    }
    private var _ProductType: BasicProductType {
        willSet {
            self.updateBackup()
        }
        didSet {
            self._ReleaseUpdateDate = Date.now
        }
    }
    private var _ProductDescription: String {
        willSet {
            self.updateBackup()
        }
        didSet {
            self._ReleaseUpdateDate = Date.now
        }
    }
    private var _discount: Double {
        willSet {
            self.updateBackup()
        }
        didSet {
            self._ReleaseUpdateDate = Date.now
        }
    }
    private var _pruchasers: [UUID] {
        didSet {
            self._pruchasers = Array(Set(_pruchasers))
        }
    }

    private var _ReleaseUpdateDate: Date
    private let _ProductPrice: Double
    //Product is create by one account, it must be merchantAccount this type
    private let _ProductOwnership: ProductOwner
    private let _ProductOwnerPointer: UUID?

    init(_ name: String, _ price: Double, _ type: BasicProductType, _ desc: String, owner: UUID? = nil) {
        //Init default value
        self._ProductName = name
        self._ProductPrice = price
        self._ProductType = type
        self._ProductDescription = desc
        self._ReleaseUpdateDate = Date.now
        //Can not change
        self.CerateDate = Date.now
        self.id = UUID()
        self._discount = 1
        //single
        self._aliveSatment = true
        self._pruchasers = []

        if let pointerID = owner {
            self._ProductOwnership = .merchantAccount
            self._ProductOwnerPointer = pointerID
        } else {
            self._ProductOwnership = .official
            self._ProductOwnerPointer = nil
        }
    }

    let CerateDate: Date
    var ReleaseUpdateDate: Date {
        self._ReleaseUpdateDate
    }
    var ProductName: String {
        self._ProductName
    }
    var ProductPrice: Double {
        self._ProductPrice * _discount
    }
    var discount: Double {
        self._discount
    }
    var ProductType: BasicProductType {
        self._ProductType
    }
    var ProductDescription: String {
        self._ProductDescription
    }
    var Pruchasers: [UUID] {
        self._pruchasers
    }

    private var staticBackupBase: [PorductOperateStruct] = []
}

extension ProductItemData: Comparable, Equatable, CustomStringConvertible {
    //Comparable: metch witch bigger?
    public static func < (lhs: ProductItemData, rhs: ProductItemData) -> Bool {
        lhs._ReleaseUpdateDate < rhs._ReleaseUpdateDate
    }

    //Equatable: metch is same?
    public static func == (lhs: ProductItemData, rhs: ProductItemData) -> Bool {
        lhs.id == rhs.id
    }

    //CustomStringConvertible
    public var description: String {
        var basicPrice: String {
            var result: String = String(self._ProductPrice)
            var appendSpace: String = ""
            let priceStr: String = String(self._ProductPrice)
            if priceStr.count < 10 {
                for _ in 0...(9 - priceStr.count) {
                    appendSpace.append(" ")
                }
            } else if priceStr.count >= 10 {
                result = String(result.prefix(10))
            }
            return appendSpace + result
        }
        var basicName: String {
            var result: String = self._ProductName
            let nameStr: String = self._ProductName
            if nameStr.count < 20 {
                for _ in 0...(19 - nameStr.count) {
                    result.append(" ")
                }
            } else if nameStr.count >= 20 {
                result = String(result.prefix(20))
            }
            return result
        }
        var basicType: String {
            var result = self._ProductType.rawValue
            var typeStr: String {
                "\(self._ProductType)"
            }
            if typeStr.count < 13 {
                for _ in 0...(12 - typeStr.count) {
                    result.append(" ")
                }
            }
            return result
        }

        return "Name: \(basicName) |  Price: \(basicPrice) | Type: \(basicType) |  Pruchaser: \(self._pruchasers.count)"
    }
}

extension ProductItemData {
    private func updateBackup() {
        let newRecord = PorductOperateStruct(ProductName: self._ProductName, discount: self._discount, ProductType: self._ProductType, ProductDescription: self._ProductDescription)
        self.staticBackupBase.insert(newRecord, at: 0)

        if self.staticBackupBase.count > 4 {
            self.staticBackupBase = Array(self.staticBackupBase.prefix(4))
        }
    }

    func setProductName(_ name: String) -> Bool {
        if name.count > 120 {
            return false
        }
        print("\(self._ProductName) already change to \(name)")
        self._ProductName = name
        return true
    }

    func setProductDescription(_ description: String) {
        self._ProductDescription = description
    }

    func setProductDiscount(_ discount: Double) throws -> Double {
        if discount > 1 || discount < 0 {
            throw ProductSetError.SetDiscountOvered
        }
        self._discount = discount
        return self.ProductPrice
    }

    func setProductType(_ type: String) throws -> String {
        for staticType in BasicProductType.allCases {
            if staticType.rawValue == type {
                self._ProductType = staticType
                return type
            }
        }

        throw ProductSetError.SetTypeIndentBasicType
    }

    func recordBacktracking() -> String {
        if self.staticBackupBase.count < 4 {
            return "Not avaliable to backup"
        }

        func restoreBackup(at index: Int) -> String {
            guard index > 0, index < 3 else {
                return "Invalid index. Enter a number between 1 and 2."
            }
            let backup = self.staticBackupBase[index - 1]
            _ProductName = backup.ProductName
            _ProductType = backup.ProductType
            _ProductDescription = backup.ProductDescription
            _discount = backup.discount
            return "Record restored successfully."
        }

        print("History:")
        print(self.staticBackupBase[0].description)
        print(self.staticBackupBase[1].description)
        print("Which backup do you want to restore (1 or 2)?")

        guard let input = readLine(), !input.isEmpty, let index = Int(input) else {
            return "Invalid input. Enter a number between 1 and 2."
        }

        return restoreBackup(at: index)
    }

    func deathmentSure() {
        self._aliveSatment = false
    }

    //get data
    func getProductStatment() -> Bool {
        self._aliveSatment
    }

    func getProductDiscount() -> Double {
        self._discount
    }

    func getProductName() -> String {
        self._ProductName
    }

    func getProductDescription() -> String {
        self._ProductDescription
    }

    func getProductType() -> BasicProductType {
        self._ProductType
    }

    func getProductPrice() -> Double {
        self._ProductPrice
    }

    func getProductCreateDate() -> Date {
        self.CerateDate
    }

    func getProductRelease() -> Date {
        self._ReleaseUpdateDate
    }

    func getProductId() -> UUID {
        self.id
    }

    func getChangeHistory() -> [PorductOperateStruct] {
        self.staticBackupBase
    }

    func getIsOwner(vaildId: UUID) -> Bool {
        if self._pruchasers.filter({ $0 == vaildId }).count == 0 {
            return false
        }
        return true
    }

    func getOwnerId() -> UUID? {
        self._ProductOwnerPointer
    }

    func appendPruchaser(userId: UUID) -> UUID {
        self._pruchasers.append(userId)
        let otherM = OtherManager()
        let ownerId = self.getOwnerId() ?? UUID(uuidString: "00000000-0000-0000-0000-000000000000")!
        let otherID = otherM.newOtherEvent(product: self.id, merchant: ownerId, creator: userId)
        print(self.description)
        return otherID
    }

}

struct PorductOperateStruct: BasicProduct, Codable, CustomStringConvertible {
    let ProductName: String
    let discount: Double
    let ProductType: BasicProductType
    let ProductDescription: String

    //Conforms to protocol: CustomStringConvertible
    var description: String {
        "Name: \(self.ProductName); Discount: \(self.discount); Type: \(self.ProductType); Description: \(self.ProductDescription)"
    }
}


// Final Product Item Model

public final class ProductItem: ProductItemData {

    func changeDiscount(_ number: Double) {
        do {
            let angle = try super.setProductDiscount(number)
            print("now price is \(angle)")
        } catch let error {
            print("Error: \(error)")
        }
    }

    func changeType(_ name: String) {
        do {
            let newType = try super.setProductType(name)
            print("Change: Product name change to \(newType)")
        } catch let error {
            print("Error: \(error)")
        }
    }
}

//Product Manager Motifier

public class ProductManager: Codable {
    public init() {
    }

    //static list
    private static var list: [ProductItem] = []

    private static var aliveList: [ProductItem] {
        Self.list.filter({ $0.getProductStatment() == true })
    }

    private static var recycle: [ProductItem] {
        Self.list.filter({ $0.getProductStatment() == false })
    }

    static var multistageList: [[BasicProductType: [ProductItem]]] {
        return BasicProductType.allCases.map { (type: BasicProductType) -> [BasicProductType: [ProductItem]] in
            let arr = aliveList.filter {
                $0.getProductType() == type
            }
            return [type: arr]
        }
    }
    var aliveProducts: [ProductItem] {
        Self.aliveList
    }
    var recycleProducts: [ProductItem] {
        Self.recycle
    }

    private func appendBasicProduct(_ name: String, _ price: Double, _ type: BasicProductType, _ description: String, _ ownerId: UUID?) throws -> UUID {
        if Self.list.firstIndex(where: { $0.getProductName() == name }) != nil {
            throw ProductManagerError.ProductCreateConflict
        }
        let newProduct: ProductItem = ProductItem(name, price, type, description, owner: ownerId)
        Self.list.append(newProduct)
        return newProduct.id
    }

    private func getProductPointer(_ index: Int) throws -> ProductItem {
        if Self.list.count <= index || index < 0 {
            throw ProductManagerError.ProductIndexOutOfRange
        }

        let pointer = withUnsafeMutablePointer(to: &Self.list[index]) { $0 }
        return pointer.pointee
    }

    private func removeBasicProductAt(index: Int) throws -> ProductItem {
        if index > 0 && index < Self.list.count {
            do {
                let productPointer: ProductItem = try self.getProductPointer(index)
                productPointer.deathmentSure()
                return productPointer
            } catch let error {
                throw error
            }
        } else {
            throw ProductManagerError.ProductIndexOutOfRange
        }
    }
}

extension ProductManager {
    func createNewProduct(name: String, price: Double, type: String, description: String, hopper: UUID? = nil) -> UUID? {
        if name.isEmpty || price == 0 || type.isEmpty || description.isEmpty {
            return nil
        }
        do {
            for staticType in BasicProductType.allCases {
                if staticType.rawValue.lowercased() == type.lowercased() {
                    let motiID = try appendBasicProduct(name, price, staticType, description, hopper)
                    return motiID
                }
            }
            return nil
        } catch let error {
            print("Error: \(error)")
            return nil
        }
    }

    public func searchAliveProduct(value: String) -> [UUID] {
        var result: [UUID] = []

        for item in Self.aliveList {
            if item.getProductName().lowercased().contains(value.lowercased()) {
                result.append(item.getProductId())
            }
        }

        print("\(result.count) product is searched")
        return Array(Set(result))
    }

    func removeProduct(index: Int) -> Bool {
        do {
            let historyProduct = try self.removeBasicProductAt(index: index)
            print("Product already delete -> \(historyProduct.getProductName())")
            return true
        } catch let error {
            print("Error: \(error)")
            return false
        }
    }

    public func analysisID(id: UUID) -> Int? {
        let resultIndex: Int? = Self.list.firstIndex(where: { $0.id == id })
        if resultIndex == nil {
            print("Error: Illegal index")
        }
        return resultIndex
    }

    public func changeProductData(index: Int, name: String? = nil, description: String? = nil, type: String? = nil, discount: Double? = nil) -> Bool {
        do {
            let productPointer = try self.getProductPointer(index)

            if let productName = name {
                _ = productPointer.setProductName(productName)
            }

            if let productDescription = description {
                productPointer.setProductDescription(productDescription)
            }

            if let productType = type {
                productPointer.changeType(productType)
            }

            if let productDiscount = discount {
                productPointer.changeDiscount(productDiscount)
            }

            return true
        } catch let error {
            print("Error: \(error)")
        }
        return false
    }

    public func getItemDescription(index: Int) -> String? {
        do {
            let productPointer = try self.getProductPointer(index)
            return productPointer.description
        } catch let error {
            print("Error: \(error)")
            return nil
        }
    }

    func getProductRecordHistory(index: Int) -> [PorductOperateStruct]? {
        do {
            let productPointer = try self.getProductPointer(index)
            return productPointer.getChangeHistory()
        } catch let error {
            print("Error: \(error)")
            return nil
        }
    }

    public func recordBackProduct(index: Int) {
        do {
            let productPointer = try self.getProductPointer(index)
            print(productPointer.recordBacktracking())
        } catch let error {
            print("Error: \(error)")
        }
    }

    func buyProductWithAccount(productId: UUID, userId: UUID) -> UUID? {
        if let product = Self.aliveList.filter({ $0.getProductId() == productId }).first {
            return product.appendPruchaser(userId: userId)
        }
        return nil
    }

    func getProductPruchasers(productId: UUID) -> [UUID] {
        guard let index = self.analysisID(id: productId) else {
            return []
        }

        do {
            let userIdSet = try self.getProductPointer(index).Pruchasers
            return userIdSet
        } catch let error {
            print("Error: \(error)")
            return []
        }
    }

    public func getProductMerchantDesc(productID: UUID) -> String? {
        guard let index = self.analysisID(id: productID) else {
            return nil
        }
        do {
            let productPointer = try self.getProductPointer(index)
            return productPointer.getProductDescription()
        } catch let error {
            print("Error: \(error)")
            return nil
        }
    }

    public func getProductByOwner(owner: UUID) -> [UUID] {
        let idArr: [ProductItem] = Self.list.filter({ $0.getOwnerId() == owner })
        let result: [UUID] = idArr.map({ $0.getProductId() })
        return result
    }

    func randomGetAliceId() -> UUID {
        let index = Int.random(in: 0...self.aliveProducts.count - 1)
        return self.aliveProducts[index].getProductId()
    }

    public static func backupAlice() -> [ProductItem] {
        Self.list
    }

    public static func reloadStaticAlice(aliceData: [ProductItem]) {
        if aliceData.isEmpty {
            print("Not avaliable to load a empty data")
        } else {
            Self.list = aliceData
            print("Product: Success to load \(aliceData.count) data")
        }
    }
}

extension ProductManager {
    func createProduct_input() -> UUID {
        repeat {
            let name: String = inputFuncses.inputString("Enter product name:")
            let price: Double = inputFuncses.inputDouble("Enter product price:")
            let type: BasicProductType = inputFuncses.inputBasicType("Enter product type:", true)
            let description: String = inputFuncses.inputString("Enter product description:", endl: "Product creation is succefull!")

            do {
                let result: UUID = try self.appendBasicProduct(name, price, type, description, nil)
                return result
            } catch let error {
                print("Error: \(error)")
            }
        } while 1 > 0
    }

    func changeProductName_input(index: Int) {
        do {
            let productPointer = try self.getProductPointer(index)
            var isOver: Bool = false
            repeat {
                let name = inputFuncses.inputString("Enter product new name:")
                if productPointer.setProductName(name) {
                    isOver.toggle()
                } else {
                    print("Name is not conforms to needed")
                }
            } while !isOver
        } catch let error {
            print("Error: \(error)")
        }
    }

    func changeProductDescription_input(index: Int) {
        do {
            let productPointer = try self.getProductPointer(index)
            productPointer.setProductDescription(inputFuncses.inputString("Enter product new description:"))
        } catch let error {
            print("Error: \(error)")
        }
    }

    func changeProducType_input(index: Int) {
        do {
            let productPointer = try self.getProductPointer(index)
            productPointer.changeType(inputFuncses.inputBasicType("Enter product new type:", true).rawValue)
        } catch let error {
            print("Error: \(error)")
        }
    }

    func changeProductDiscount_input(index: Int) {
        do {
            let productPointer = try self.getProductPointer(index)
            var isOver: Bool = false
            repeat {
                let discount = inputFuncses.inputDouble("Enter product discount")
                if discount > 0 && discount <= 1 {
                    productPointer.changeDiscount(discount)
                    isOver.toggle()
                } else {
                    print("Number must between 0 and 1")
                }
            } while !isOver
        } catch let error {
            print("Error: \(error)")
        }
    }
}


enum ProductSetError: String, Error {
    case SetNameOverError = "Name cannot over 120 char"
    case SetDiscountOvered = "Discount just betwine 0 and 1"
    case SetTypeIndentBasicType = "Cannot support type not conforms to 'BasicProductType'"
}

enum ProductManagerError: String, Error {
    //Basic Errors
    case ProductCreateConflict = "This information has somthing conflict form list"
    case ProductCreateInitError = "This rawValue is it self"
    case ProductIDNotConform = "This id is out of list"
    case ProductIndexOutOfRange = "Your are provide an error index"
    //Pointer Errors
    case PointerError = "Illegal pointing to memory errors"
}

public enum BasicProductType: String, Codable {
    case Phone, Clothing, Food, Furniture, Clock, Jewelry, Electrical, MaternalBaby, Bags, OutdoorSports, PetSupplies

    static func getProductTypesList() -> [String] { allCases.map { type in type.rawValue } }
}

extension BasicProductType: Sequence {
    static var allCases: [BasicProductType] = [.Phone, .Clothing, .Food, .Furniture, .Clock, .Jewelry, .Electrical, .MaternalBaby, .Bags, .OutdoorSports, .PetSupplies]

    public typealias EnumeratedIterator = BasicProductIterator

    public func makeIterator() -> EnumeratedIterator { BasicProductIterator() }

    public class BasicProductIterator: IteratorProtocol {
        var currentIndex = -1
        let allValues = BasicProductType.allCases

        public func next() -> BasicProductType? {
            currentIndex += 1
            return currentIndex < allValues.count ? allValues[currentIndex] : nil
        }
    }
}

enum ProductOwner: String, Codable {
    case official, standardAccount, merchantAccount
}

enum OtherState: String, Codable, CaseIterable {
    case Progress, Paused, Done, Canceled

    static var allCases: [OtherState] = [.Progress, .Paused, .Done, .Canceled]
}


// Account Manager Motifier

public class BasicAccount: Identifiable, Codable, Comparable, Equatable {
    public let id: UUID
    private var _accountName: String
    private var _password: String
    private let _cerateDate: Date

    private var _disabled: Bool
    private var _disabledReason: String

    init(name: String, password: String) {
        self._accountName = name
        self._cerateDate = .now
        self.id = UUID()
        self._password = password
        //stander
        self._disabled = false
        self._disabledReason = ""
    }

    var cerateDate: Date {
        self._cerateDate
    }

    var disabled: Bool {
        self._disabled
    }

    var disabledReason: String {
        self._disabledReason
    }

    public func getAccountId() -> UUID {
        self.id
    }

    func getAccountName() -> String {
        self._accountName
    }

    func setAccountName(name: String) {
        self._accountName = name
    }

    func setAccountPassword(vaild: String, newPwd: String) -> Bool {
        if vaild.isEmpty || newPwd.isEmpty || vaild != self._password || newPwd.count > 16 {
            return false
        }
        self._password = newPwd
        return true
    }

    func setDsiabled(isBand: Bool, reason: String) {
        self._disabled = isBand
        self._disabledReason = reason
    }

    public static func < (lhs: BasicAccount, rhs: BasicAccount) -> Bool {
        lhs.cerateDate < rhs.cerateDate
    }

    public static func == (lhs: BasicAccount, rhs: BasicAccount) -> Bool {
        lhs.getAccountId() == rhs.getAccountId()
    }
}

public class standardAccount: BasicAccount {
    private let PManager: ProductManager = ProductManager()
    private var _orderGoods: [UUID] {
        var result: [UUID] = []
        for item in PManager.aliveProducts.filter({ $0.getIsOwner(vaildId: self.id) }) {
            result.append(item.getProductId())
        }
        return result
    }
    private var _accountType: ProductOwner = .standardAccount

    public func buyProduct(productID: UUID) -> UUID? {
        if let result = PManager.buyProductWithAccount(productId: productID, userId: self.id) {
            return result
        }
        return nil
    }

    func getOrderGoodsCounts() -> Int {
        self._orderGoods.count
    }
}

public class merchantAccount: BasicAccount, CustomStringConvertible {
    private let PManager: ProductManager = ProductManager()
    private var _PublishedProducts: [ProductItem] {
        PManager.aliveProducts.filter({ $0.getOwnerId() == self.id })
    }
    private var _accountType: ProductOwner = .merchantAccount

    //Conforms to CustomStringConvertible
    public var description: String {
        "Merchant Name: \(self.getAccountName()); Products: \(self._PublishedProducts.count);"
    }

    public func newProduct(name: String, price: Double, type: String, description: String) -> UUID? {
        return self.PManager.createNewProduct(name: name, price: price, type: type, description: description, hopper: self.getAccountId())
    }

    func removeProduct(id: UUID) -> Bool {
        if let index = self.PManager.analysisID(id: id) {
            return self.PManager.removeProduct(index: index)
        }
        return false
    }

    func changeProductData(id: UUID, name: String? = nil, description: String? = nil, type: String? = nil, discount: Double? = nil) -> Bool {
        if let index = self.PManager.analysisID(id: id) {
            let result = self.PManager.changeProductData(index: index, name: name, description: description, type: type, discount: discount)
            return result
        }
        return false
    }

    func getProductSet() -> [ProductItem] {
        PManager.aliveProducts.filter({ $0.getOwnerId() == self.id })
    }

    func recordBackData(id: UUID) {
        if let index = self.PManager.analysisID(id: id) {
            self.PManager.recordBackProduct(index: index)
        }
    }
}

//Manager

public class StandardAccountManager: Codable {
    public init() {
    }

    private static var _standardList: [standardAccount] = []

    static var standardAliveList: [standardAccount] {
        Self._standardList.filter({ $0.disabled == false })
    }
    static var standardDsabledList: [standardAccount] {
        Self._standardList.filter({ $0.disabled == true })
    }

    public func newStandardAccount(username: String, password: String) -> UUID? {
        if !Self._standardList.filter({ $0.getAccountName() == username }).isEmpty {
            print(Self._standardList.filter({ $0.getAccountName() == username }).isEmpty)
            return nil
        }
        let cashAccount = standardAccount(name: username, password: password)
        Self._standardList.append(cashAccount)
        return cashAccount.getAccountId()
    }

    public func signWithStandard(username: String, password: String) -> standardAccount? {
        let accountSet = Self.standardAliveList.filter({ $0.getAccountName() == username })
        if accountSet.count == 1 {
            if let id = accountSet.first?.id, let pointer = analysisStandardID(accountId: id) {
                return pointer
            }
            return nil
        } else {
            return nil
        }
    }

    public func analysisStandardID(accountId: UUID) -> standardAccount? {
        guard let index = Self._standardList.firstIndex(where: { $0.getAccountId() == accountId }) else {
            return nil
        }
        let accountPointer = withUnsafeMutablePointer(to: &Self._standardList[index], { $0 })
        return accountPointer.pointee
    }

    func disabledAccount(accountId: UUID, cause: String) -> Bool {
        guard let account = self.analysisStandardID(accountId: accountId) else {
            return false
        }
        account.setDsiabled(isBand: true, reason: cause)
        return true
    }

    public static func backupAlice() -> [standardAccount] {
        Self._standardList
    }

    public static func reloadStaticAlice(aliceData: [standardAccount]) {
        if aliceData.isEmpty {
            print("Not avaliable to load a empty data")
        } else {
            print("Standard: Success to load \(aliceData.count) data")
            Self._standardList = aliceData
        }
    }
}

public class MerchantAccountManager: Codable {
    public init() {
    }

    private static var _merchantList: [merchantAccount] = []

    static var merchantAliveList: [merchantAccount] {
        Self._merchantList.filter({ $0.disabled == false })
    }
    static var merchantDsabledList: [merchantAccount] {
        Self._merchantList.filter({ $0.disabled == true })
    }

    var merchantList: [merchantAccount] {
        Self._merchantList
    }

    public func newMerchantAccount(username: String, password: String) -> UUID? {
        if !Self._merchantList.filter({ $0.getAccountName() == username }).isEmpty {
            return nil
        }
        let cashAccount = merchantAccount(name: username, password: password)
        Self._merchantList.append(cashAccount)
        return cashAccount.getAccountId()
    }

    public func signWithMerchant(username: String, password: String) -> merchantAccount? {
        let accountSet = Self.merchantAliveList.filter({ $0.getAccountName() == username })
        if accountSet.count == 1 {
            if let id = accountSet.first?.id, let pointer = self.analysisMerchantID(accountId: id) {
                return pointer
            }
            return nil
        } else {
            return nil
        }
    }

    public func analysisMerchantID(accountId: UUID) -> merchantAccount? {
        guard let index = Self._merchantList.firstIndex(where: { $0.getAccountId() == accountId }) else {
            return nil
        }
        let accountPointer = withUnsafeMutablePointer(to: &Self._merchantList[index], { $0 })
        return accountPointer.pointee
    }

    func disabledAccount(accountId: UUID, cause: String) -> Bool {
        guard let account = self.analysisMerchantID(accountId: accountId) else {
            return false
        }
        account.setDsiabled(isBand: true, reason: cause)
        return true
    }

    public static func backupAlice() -> [merchantAccount] {
        Self._merchantList
    }

    public static func reloadStaticAlice(aliceData: [merchantAccount]) {
        if aliceData.isEmpty {
            print("Not avaliable to load a empty data")
        } else {
            print("Merchant: Success to load \(aliceData.count) data")
            Self._merchantList = aliceData
        }
    }
}


// Main Function, Way

func main() {
    let startTimestamp = Int(Date().timeIntervalSince1970)
    print("[\(Date.now)] Repercentable Supermarket Application preparing...")
    let manager: ProductManager = ProductManager()
    let standard: StandardAccountManager = StandardAccountManager()
    let merchant: MerchantAccountManager = MerchantAccountManager()
    let otherM: OtherManager = OtherManager()


    // Ckect to load json data when program start
    var networkingStatus: Bool {
        isInternetAvailable()
    }

    var aliceBackData: savedAliceData? = nil
    // let aliceBackData: savedAliceData? = loadJsonDataFromURL()

    // Loading will open when service is available, but now is not served. These code will open when server is run.
    if let buffer = loadJsonDataFromURL(url: "https://domain.com/static/file/repSupermarketSavedAliceData.json") {
        aliceBackData = buffer
    } else {
        aliceBackData = loadJsonDataFromURL()
    }

    var suCash: Bool = true

    if let alice = aliceBackData {
        ProductManager.reloadStaticAlice(aliceData: alice.productArr)
        OtherManager.reloadStaticAlice(aliceData: alice.otherArr)
        StandardAccountManager.reloadStaticAlice(aliceData: alice.standardAccountArr)
        MerchantAccountManager.reloadStaticAlice(aliceData: alice.merchantAccountArr)
    } else {
        print("Error occurred while loading Alice data.")
        suCash = false
    }

    if suCash {
        let startedTimestamp = Int(Date().timeIntervalSince1970)
        print("[\(Date.now)] Repercentable Supermarket Application started(using \(startedTimestamp - startTimestamp)ms)")
        repeat {
            let startEvent = inputFuncses.inputString("Start: enter witch you wanna do(standard / merchant)")

            if startEvent.lowercased() == "exit" {
                let finishedTimestamp = Int(Date().timeIntervalSince1970)
                print("[\(Date.now)] Repercentable Supermarket Application finished(using \(finishedTimestamp - startTimestamp)ms)")
                suCash = false
                continue
            } else if startEvent.lowercased().first == "m" {
                func createProduct(operateAccount: merchantAccount) {
                    print(operateAccount.description)
                    var isDone: Bool = false
                    repeat {
                        let event = inputFuncses.inputString("View mains or cerate product:")
                        if event == "done" {
                            isDone = true
                        } else if event == "main" {
                            let datas = manager.getProductByOwner(owner: operateAccount.getAccountId())
                            for productId in datas {
                                if let index = manager.analysisID(id: productId), let desc = manager.getItemDescription(index: index) {
                                    let num = datas.firstIndex(where: { $0 == productId })!
                                    var showNum: String {
                                        let showIndex = String(num + 1)
                                        if showIndex.count == 1 {
                                            return "00\(showIndex)"
                                        } else if showIndex.count == 2 {
                                            return "0\(showIndex)"
                                        }
                                        return showIndex
                                    }

                                    print("\(showNum): \(desc)")
                                } else {
                                    print("Faild to load this product: \(productId)")
                                }
                            }

                            let operateIndex = Int(inputFuncses.inputDouble("Select a product or past"))
                            if operateIndex < (operateIndex + 1) && operateIndex > 0 {
                                if let desc = manager.getProductMerchantDesc(productID: datas[operateIndex - 1]), let productIndex = manager.analysisID(id: datas[operateIndex - 1]) {
                                    let productEasyDesc = manager.getItemDescription(index: productIndex) ?? "No search product and description"
                                    print(productEasyDesc)
                                    print(desc)

                                    let changeOperate = inputFuncses.inputString("Information: You wanna change or select to record:")
                                    if changeOperate.lowercased().contains("record") || changeOperate.lowercased() == "backup" {
                                        manager.recordBackProduct(index: productIndex)
                                    } else if changeOperate.lowercased().contains("change") || changeOperate.lowercased().contains("set") {
                                        let name = inputFuncses.inputString("Enter new name:")
                                        let description = inputFuncses.inputString("Enter new description:")
                                        let type = inputFuncses.inputBasicType("Enter product type:", true).rawValue
                                        let discount = inputFuncses.inputDouble("Enter new discount:")
                                        let result = manager.changeProductData(index: productIndex, name: name, description: description, type: type, discount: discount)
                                        if result {
                                            print("Changes is applied!")
                                            print(productEasyDesc)
                                        } else {
                                            print("Change is error occurred!")
                                        }
                                    }
                                } else {
                                    print("Error to load this index)")
                                }
                            }
                        } else {
                            let description = inputFuncses.inputString("Enter product description:")
                            let price = inputFuncses.inputDouble("Enter product price:")
                            let type = inputFuncses.inputBasicType("Enter product type:", true).rawValue
                            let name = inputFuncses.inputString("Enter product name")
                            let product = operateAccount.newProduct(name: name, price: price, type: type, description: description)
                            if let product = product,
                                let index = manager.analysisID(id: product),
                                let desc = manager.getItemDescription(index: index)
                            {
                                print(desc)
                            } else {
                                print("faild to create")
                            }
                        }
                    } while !isDone
                }

                let operate = inputFuncses.inputString("create new account(1) or sign in(2)")
                if operate.lowercased() == "1" || operate.lowercased().contains("sign up") || operate.lowercased().contains("register") {
                    //create
                    if let id = merchant.newMerchantAccount(username: inputFuncses.inputString("New account name:"), password: String(cString: getpass("New account password:"))), let operateMerchant = merchant.analysisMerchantID(accountId: id) {
                        createProduct(operateAccount: operateMerchant)
                    } else {
                        print("account create is failed")
                    }
                } else if operate == "2" || operate.lowercased().contains("sign in") || operate.lowercased().contains("login") {
                    //sign in
                    if let operateMerchant = merchant.signWithMerchant(username: inputFuncses.inputString("Account name:"), password: String(cString: getpass("Account password:"))) {
                        createProduct(operateAccount: operateMerchant)
                    } else {
                        print("Account message is error")
                    }
                } else {
                    print("Failed to do next step")
                }
            } else if startEvent.lowercased().first == "s" {
                func operateProduct(operateAccount: standardAccount? = nil) {

                    var isDone: Bool = false
                    repeat {
                        let command = inputFuncses.inputString("Enter what you wanna search(product)")
                        if command.lowercased() == "done" {
                            isDone = true
                        } else {
                            let resultId = manager.searchAliveProduct(value: command)
                            if resultId.isEmpty {
                                print("No product searched")
                            } else {
                                for id in resultId {
                                    if let product = manager.analysisID(id: id) {
                                        let num = resultId.firstIndex(where: { $0 == id })
                                        var showNum: String {
                                            if let num = num {
                                                let numStr = String(num + 1)
                                                return numStr.count == 1 ? "00\(numStr)" : "0\(numStr)"
                                            } else {
                                                return "000"
                                            }
                                        }
                                        let errorSearch: String = "No search with \(id)"
                                        let showDesc = manager.getItemDescription(index: product)
                                        print("\(showNum): \(showDesc ?? errorSearch)")
                                    } else {
                                        print("\(id) this product is no defind")
                                    }
                                }

                                if let sectAccout = operateAccount {
                                    let operateIdIndex = Int(inputFuncses.inputDouble("Whitch one you wanna buy(1...)"))
                                    if operateIdIndex < 1 || operateIdIndex > resultId.count {
                                        print("This index out of list")
                                    } else {
                                        if let otherId = sectAccout.buyProduct(productID: resultId[operateIdIndex - 1]) {
                                            print(otherM.getOtherDetail(otherId: otherId) ?? "Can not view product detail")
                                        } else {
                                            print("Buy this product is not success")
                                        }
                                    }
                                }
                            }
                        }
                    } while !isDone
                }

                let operate = inputFuncses.inputString("Create new account(1) or sign in(2), however you can use guest(3) in limit mode")
                if operate == "1" || operate.lowercased().contains("sign up") || operate.lowercased().contains("register") {
                    //create account
                    if let Id = standard.newStandardAccount(username: inputFuncses.inputString("New account name:"), password: String(cString: getpass("New account password:"))), let operateAccount = standard.analysisStandardID(accountId: Id) {
                        operateProduct(operateAccount: operateAccount)
                    } else {
                        print("account create is failed")
                    }
                } else if operate == "2" || operate.lowercased().contains("sign in") || operate.lowercased().contains("login") {
                    //sign in
                    if let operateAccount = standard.signWithStandard(username: inputFuncses.inputString("Account name:"), password: String(cString: getpass("Account password:"))) {
                        operateProduct(operateAccount: operateAccount)
                    } else {
                        print("Account message is error")
                    }
                } else if operate == "3" || operate.lowercased() == "guest" {
                    operateProduct()
                } else {
                    print("Failed to do next step")
                }
            } else if startEvent.lowercased().contains("admin") {
                print("Administor model is developing")
            } else if startEvent.lowercased() == "restore" {
                let restoreFilePath = inputFuncses.inputString("Enter the specific json file to restore: ")
                let _ = restoreBackupAliceData(restoreFileName: restoreFilePath, targetFileName: ".repSupermarketSavedAliceData.json")
            }
        } while suCash
    }

    //Create a Alice to save data to json file when program done
    print("[\(Date.now)] Repercentable Supermarket Application finished(using \(Int(Date().timeIntervalSince1970) - startTimestamp)ms)")
    print("Saving data to json file...")

    // Save data to json file
    let standardAccountArr = StandardAccountManager.backupAlice()
    let merchantAccountArr = MerchantAccountManager.backupAlice()
    let productArr = ProductManager.backupAlice()
    let otherArr = OtherManager.backupAlice()
    let endAlice = savedAliceData(standardAccountArr: standardAccountArr, merchantAccountArr: merchantAccountArr, productArr: productArr, otherArr: otherArr)
    saveOtherManagerToFile(endAlice)

    print("Success to save data to json file")
    print("End of program")
}

// Start
main()
