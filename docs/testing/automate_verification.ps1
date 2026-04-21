param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$StartApplication,
    [switch]$SkipUnitTests,
    [string]$ReportPath = "docs/testing/VERIFICATION_RESULTS.md"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent (Split-Path -Parent $scriptDir)
$startupProcess = $null
$applicationProcessId = $null
$runUnitTests = -not $SkipUnitTests

function Write-Section {
    param([string]$Message)

    Write-Host ""
    Write-Host "== $Message ==" -ForegroundColor Cyan
}

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Read-ErrorBody {
    param($Response)

    if (-not $Response) {
        return $null
    }

    try {
        $reader = New-Object System.IO.StreamReader($Response.GetResponseStream())
        try {
            return $reader.ReadToEnd()
        }
        finally {
            $reader.Dispose()
        }
    }
    catch {
        return $null
    }
}

function Invoke-JsonRequest {
    param(
        [string]$Method,
        [string]$Uri,
        $Payload
    )

    $body = $null
    if ($null -ne $Payload) {
        $body = $Payload | ConvertTo-Json -Depth 20
    }

    if ($body) {
        return Invoke-RestMethod -Method $Method -Uri $Uri -ContentType "application/json" -Body $body
    }

    return Invoke-RestMethod -Method $Method -Uri $Uri
}

function Wait-ForApplication {
    param([string]$HealthUrl)

    $deadline = (Get-Date).AddSeconds(90)
    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-WebRequest -UseBasicParsing $HealthUrl | Out-Null
            return
        }
        catch {
            Start-Sleep -Seconds 2
        }
    }

    throw "Application did not become ready at $HealthUrl within 90 seconds."
}

function Get-ListeningProcessId {
    param([int]$Port)

    try {
        $pattern = "^\s*TCP\s+\S+:$Port\s+\S+\s+LISTENING\s+(\d+)\s*$"
        $line = netstat -ano | Select-String $pattern | Select-Object -First 1
        if (-not $line) {
            return $null
        }

        $match = [regex]::Match($line.ToString(), $pattern)
        if (-not $match.Success) {
            return $null
        }

        return [int]$match.Groups[1].Value
    }
    catch {
        return $null
    }
}

function Start-LocalApplication {
    $logPath = Join-Path $repoRoot "docs/testing/functional_verification_app.log"
    $command = "Set-Location '$repoRoot'; mvn spring-boot:run *> '$logPath'"
    $port = [uri]$BaseUrl
    $existingPid = Get-ListeningProcessId -Port $port.Port

    $process = Start-Process `
        -FilePath "C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe" `
        -ArgumentList "-NoProfile", "-Command", $command `
        -WorkingDirectory $repoRoot `
        -PassThru

    Wait-ForApplication -HealthUrl "$BaseUrl/api/metadata/comparators"

    $listenerPid = Get-ListeningProcessId -Port $port.Port
    return [pscustomobject]@{
        WrapperProcess = $process
        ListenerProcessId = if ($listenerPid -and ($listenerPid -ne $existingPid)) { $listenerPid } else { $null }
    }
}

function Stop-LocalApplication {
    param(
        $Process,
        [int]$ListenerProcessId
    )

    if ($null -eq $Process) {
        if ($ListenerProcessId) {
            Stop-Process -Id $ListenerProcessId -Force -ErrorAction SilentlyContinue
        }
        return
    }

    try {
        if ($ListenerProcessId) {
            Stop-Process -Id $ListenerProcessId -Force -ErrorAction SilentlyContinue
        }

        Stop-Process -Id $Process.Id -Force -ErrorAction SilentlyContinue
    }
    catch {
        Write-Warning "Could not stop local application cleanly: $($_.Exception.Message)"
    }
}

function Run-Case {
    param(
        [string]$Name,
        [scriptblock]$Action
    )

    try {
        $details = & $Action
        return [pscustomobject]@{
            Name = $Name
            Status = "PASS"
            Details = $details
        }
    }
    catch {
        $response = $_.Exception.Response
        $statusCode = $null
        if ($response) {
            try {
                $statusCode = [int]$response.StatusCode
            }
            catch {
                $statusCode = $null
            }
        }

        return [pscustomobject]@{
            Name = $Name
            Status = "FAIL"
            Details = $_.Exception.Message
            HttpStatus = $statusCode
            Body = Read-ErrorBody -Response $response
        }
    }
}

function Write-MarkdownReport {
    param(
        [string]$OutputPath,
        $Results,
        [int]$Passed,
        [int]$Failed,
        $UnitTestResult
    )

    $resolvedPath = if ([System.IO.Path]::IsPathRooted($OutputPath)) {
        $OutputPath
    }
    else {
        Join-Path $repoRoot $OutputPath
    }

    $reportDir = Split-Path -Parent $resolvedPath
    if ($reportDir) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }

    $generatedAt = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss zzz")
    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("# Functional Verification Report")
    $lines.Add("")
    $lines.Add("Generated on: $generatedAt")
    $lines.Add("")
    $lines.Add(('Base URL: `' + $BaseUrl + '`'))
    $lines.Add("")
    $lines.Add("## Summary")
    $lines.Add("")
    $lines.Add("- Passed: $Passed")
    $lines.Add("- Failed: $Failed")
    $lines.Add("- Run unit tests: $runUnitTests")
    $lines.Add("- Started application: $StartApplication")
    $lines.Add("")

    if ($UnitTestResult) {
        $lines.Add("## Unit Test Suite")
        $lines.Add("")
        $lines.Add("- Status: $($UnitTestResult.Status)")
        $lines.Add("- Details: $($UnitTestResult.Details)")
        $lines.Add("")
    }

    $lines.Add("## Results")
    $lines.Add("")

    foreach ($result in $Results) {
        $statusIcon = if ($result.Status -eq "PASS") { "PASS" } else { "FAIL" }
        $lines.Add("### $($result.Name)")
        $lines.Add("")
        $lines.Add("- Status: $statusIcon")
        $lines.Add("- Details: $($result.Details)")
        if ($null -ne $result.HttpStatus) {
            $lines.Add("- HTTP status: $($result.HttpStatus)")
        }
        if ($result.Body) {
            $lines.Add("- Response body:")
            $lines.Add("")
            $lines.Add('```json')
            $lines.Add(($result.Body | Out-String).Trim())
            $lines.Add('```')
        }
        $lines.Add("")
    }

    Set-Content -Path $resolvedPath -Value $lines -Encoding UTF8
    return $resolvedPath
}

$cases = @(
    @{
        Name = "Case 1.1 - List all Transactions"
        Action = {
            $response = Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/execute" -Payload @{
                select = @(
                    @{ entity = "transaction"; field = "txnId" },
                    @{ entity = "transaction"; field = "txnType" },
                    @{ entity = "transaction"; field = "amount" },
                    @{ entity = "transaction"; field = "currency" },
                    @{ entity = "transaction"; field = "status" }
                )
            }

            $rows = @($response)
            Assert-True ($rows.Count -ge 1) "Expected at least one transaction row."
            "rows=$($rows.Count)"
        }
    },
    @{
        Name = "Case 1.2 - Filter by Currency"
        Action = {
            $response = Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/execute" -Payload @{
                select = @(
                    @{ entity = "transaction"; field = "txnId" },
                    @{ entity = "transaction"; field = "amount" },
                    @{ entity = "transaction"; field = "currency" }
                )
                filters = @{ entity = "transaction"; field = "currency"; comparator = "equals"; value = "USD" }
            }

            $rows = @($response)
            Assert-True ($rows.Count -ge 1) "Expected USD transactions."
            $currencies = @($rows | ForEach-Object { $_.currency } | Select-Object -Unique)
            Assert-True (($currencies.Count -eq 1) -and ($currencies[0] -eq "USD")) "Expected only USD rows but got: $($currencies -join ', ')."
            "rows=$($rows.Count) currencies=$($currencies -join ',')"
        }
    },
    @{
        Name = "Case 2.1 - Multi-hop Join"
        Action = {
            $response = Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/execute" -Payload @{
                select = @(
                    @{ entity = "transaction"; field = "txnId" },
                    @{ entity = "issuer"; field = "partyName" },
                    @{ entity = "issuer"; field = "country" }
                )
                filters = @{ entity = "issuer"; field = "isActive"; comparator = "equals"; value = 1 }
            }

            $rows = @($response)
            Assert-True ($rows.Count -ge 1) "Expected issuer rows from the multi-hop join case."
            $countries = @($rows | ForEach-Object { $_.country } | Select-Object -Unique)
            "rows=$($rows.Count) countries=$($countries -join ',')"
        }
    },
    @{
        Name = "Case 3.1 - Nested Boolean Logic"
        Action = {
            $response = Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/build" -Payload @{
                select = @(
                    @{ entity = "transaction"; field = "txnId" },
                    @{ entity = "transaction"; field = "status" },
                    @{ entity = "transaction"; field = "amount" }
                )
                filters = @{
                    operator = "OR"
                    conditions = @(
                        @{
                            operator = "AND"
                            conditions = @(
                                @{ entity = "transaction"; field = "status"; comparator = "equals"; value = "SETTLED" },
                                @{ entity = "transaction"; field = "amount"; comparator = "greaterThan"; value = 5000000 }
                            )
                        },
                        @{ entity = "transaction"; field = "status"; comparator = "equals"; value = "PENDING" }
                    )
                }
            }

            Assert-True ($response.sql -match "WHERE") "Expected generated SQL to contain WHERE."
            Assert-True ($response.sql -match "OR") "Expected generated SQL to contain OR."
            $response.sql
        }
    },
    @{
        Name = "Case 3.2 - Pagination and Sorting"
        Action = {
            $response = Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/execute" -Payload @{
                select = @(
                    @{ entity = "transaction"; field = "txnId" },
                    @{ entity = "transaction"; field = "amount" }
                )
                sorting = @(
                    @{ entity = "transaction"; field = "amount"; direction = "DESC" }
                )
                maxResults = 5
            }

            $rows = @($response)
            Assert-True ($rows.Count -le 5) "Expected at most 5 rows but got $($rows.Count)."
            $amounts = @($rows | ForEach-Object { [decimal]$_.amount })
            $sorted = @($amounts | Sort-Object -Descending)
            Assert-True (-not (Compare-Object -ReferenceObject $amounts -DifferenceObject $sorted)) "Rows are not sorted by amount descending."
            "rows=$($rows.Count) topAmount=$($amounts[0])"
        }
    },
    @{
        Name = "Case 4.1 - Unknown Field Error"
        Action = {
            try {
                Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/execute" -Payload @{
                    select = @(
                        @{ entity = "transaction"; field = "txnId" },
                        @{ entity = "transaction"; field = "non_existent_field" }
                    )
                } | Out-Null

                throw "Expected HTTP 400 but request succeeded."
            }
            catch {
                $response = $_.Exception.Response
                if (-not $response) {
                    throw
                }

                $body = Read-ErrorBody -Response $response
                $statusCode = [int]$response.StatusCode
                Assert-True ($statusCode -eq 400) "Expected HTTP 400 but got $statusCode."
                Assert-True ($body -match "non_existent_field") "Expected the invalid field name in the error body."
                "status=$statusCode"
            }
        }
    },
    @{
        Name = "Case 5.1 - Metadata Entities"
        Action = {
            $response = Invoke-JsonRequest -Method "Get" -Uri "$BaseUrl/api/metadata/entities"
            $entities = @($response | ForEach-Object { $_.entity })
            foreach ($requiredEntity in @("transaction", "instrument", "party")) {
                Assert-True ($entities -contains $requiredEntity) "Missing metadata entity '$requiredEntity'."
            }
            "entities=$($entities -join ',')"
        }
    },
    @{
        Name = "Case 5.2 - Metadata Comparators"
        Action = {
            $response = Invoke-JsonRequest -Method "Get" -Uri "$BaseUrl/api/metadata/comparators"
            Assert-True ($null -ne $response.string) "Expected string comparators."
            Assert-True ($null -ne $response.number) "Expected number comparators."
            "string=$($response.string.Count) number=$($response.number.Count)"
        }
    },
    @{
        Name = "Case 5.3 - Query Validation"
        Action = {
            $response = Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/validate" -Payload @{
                select = @(
                    @{ entity = "transaction"; field = "txnId" }
                )
                filters = @{ entity = "transaction"; field = "status"; comparator = "equals"; value = "SETTLED" }
            }

            Assert-True ($response.valid) "Expected valid=true."
            "valid=$($response.valid)"
        }
    },
    @{
        Name = "Case 5.4 - SQL Generation"
        Action = {
            $response = Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/build" -Payload @{
                select = @(
                    @{ entity = "transaction"; field = "txnId" }
                )
                filters = @{ entity = "transaction"; field = "status"; comparator = "equals"; value = "SETTLED" }
            }

            Assert-True ($response.sql -match "SELECT") "Expected generated SQL to contain SELECT."
            Assert-True ($response.sql -match "STATUS") "Expected generated SQL to contain STATUS."
            $response.sql
        }
    },
    @{
        Name = "Case 5.5 - Data Execution"
        Action = {
            $response = Invoke-JsonRequest -Method "Post" -Uri "$BaseUrl/api/query/execute" -Payload @{
                select = @(
                    @{ entity = "transaction"; field = "txnId" }
                )
                filters = @{ entity = "transaction"; field = "status"; comparator = "equals"; value = "SETTLED" }
            }

            $rows = @($response)
            Assert-True ($rows.Count -ge 1) "Expected rows for settled transactions."
            "rows=$($rows.Count)"
        }
    }
)

try {
    Write-Section "Functional Verification"
    Write-Host "Base URL: $BaseUrl"
    $unitTestResult = $null

    if ($runUnitTests) {
        Write-Section "Running Maven Test Suite"
        Push-Location $repoRoot
        try {
            & mvn test
            $unitTestResult = [pscustomobject]@{
                Name = "Maven Test Suite"
                Status = "PASS"
                Details = "mvn test completed successfully."
            }
            Write-Host "[PASS] Maven Test Suite" -ForegroundColor Green
            Write-Host "  mvn test completed successfully."
        }
        catch {
            $unitTestResult = [pscustomobject]@{
                Name = "Maven Test Suite"
                Status = "FAIL"
                Details = $_.Exception.Message
            }
            Write-Host "[FAIL] Maven Test Suite" -ForegroundColor Red
            Write-Host ("  {0}" -f $_.Exception.Message)
            throw
        }
        finally {
            Pop-Location
        }
    }

    if ($StartApplication) {
        Write-Section "Starting Local Application"
        $startupInfo = Start-LocalApplication
        $startupProcess = $startupInfo.WrapperProcess
        $applicationProcessId = $startupInfo.ListenerProcessId
        Write-Host "Started process tree root PID: $($startupProcess.Id)"
        if ($applicationProcessId) {
            Write-Host "Application listener PID: $applicationProcessId"
        }
    }
    else {
        Write-Section "Checking Application Availability"
        Wait-ForApplication -HealthUrl "$BaseUrl/api/metadata/comparators"
    }

    Write-Section "Executing Functional Cases"
    $results = foreach ($case in $cases) {
        $result = Run-Case -Name $case.Name -Action $case.Action
        $color = if ($result.Status -eq "PASS") { "Green" } else { "Red" }
        Write-Host ("[{0}] {1}" -f $result.Status, $result.Name) -ForegroundColor $color
        Write-Host ("  {0}" -f $result.Details)
        if ($result.Body) {
            Write-Host ("  body={0}" -f $result.Body)
        }
        $result
    }

    Write-Section "Summary"
    $passed = @($results | Where-Object Status -eq "PASS").Count
    $failed = @($results | Where-Object Status -eq "FAIL").Count
    Write-Host "Passed: $passed"
    Write-Host "Failed: $failed"

    Write-Section "Writing Markdown Report"
    $resolvedReportPath = Write-MarkdownReport -OutputPath $ReportPath -Results $results -Passed $passed -Failed $failed -UnitTestResult $unitTestResult
    Write-Host "Report: $resolvedReportPath"

    if ($failed -gt 0) {
        exit 1
    }
}
finally {
    if ($StartApplication) {
        Write-Section "Stopping Local Application"
        Stop-LocalApplication -Process $startupProcess -ListenerProcessId $applicationProcessId
    }
}
