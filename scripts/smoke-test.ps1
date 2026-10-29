param([string]$BaseUrl='http://127.0.0.1:8080')
$ErrorActionPreference='Stop'
$results=@()
foreach($scenario in @('NORMAL_ORDER','DUPLICATE_ORDER','PAYMENT_FAILURE','TIMEOUT_WITH_IDEMPOTENCY')){
 $run=Invoke-RestMethod -Uri "$BaseUrl/api/simulations/run" -Method Post -ContentType 'application/json' -Body (@{scenario=$scenario}|ConvertTo-Json)
 $expected=if($scenario -eq 'PAYMENT_FAILURE'){0}elseif($scenario -eq 'DUPLICATE_ORDER'){2}else{1}
 if($run.orders.Count -ne $expected){throw "$scenario returned the wrong order count"}
 $events=Invoke-RestMethod -Uri "$BaseUrl/api/simulations/$($run.id)/events"
 if($events.Count -ne $run.events.Count){throw 'Stored event count differs from creation response.'}
 $verification=Invoke-RestMethod -Uri "$BaseUrl/api/verification/$($run.id)" -Method Post
 if($verification.status -ne 'PASSED'){throw "$scenario verification failed"}
 $results+=@{scenario=$scenario;runId=$run.id;orders=$run.orders.Count;events=$events.Count;verification=$verification.status}
}
$results|ConvertTo-Json -Depth 5
